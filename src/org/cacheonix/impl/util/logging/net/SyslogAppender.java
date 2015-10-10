/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cacheonix.impl.util.logging.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.cacheonix.impl.util.logging.AppenderSkeleton;
import org.cacheonix.impl.util.logging.Layout;
import org.cacheonix.impl.util.logging.helpers.SyslogQuietWriter;
import org.cacheonix.impl.util.logging.helpers.SyslogWriter;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

// Contributors: Yves Bossel <ybossel@opengets.cl>
//               Christopher Taylor <cstaylor@pacbell.net>

/**
 * Use SyslogAppender to send log messages to a remote syslog daemon.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 */
@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public final class SyslogAppender extends AppenderSkeleton {
   // The following constants are extracted from a syslog.h file
   // copyrighted by the Regents of the University of California
   // I hope nobody at Berkley gets offended.

   /**
    * Kernel messages
    */
   public static final int LOG_KERN = 0;

   /**
    * Random user-level messages
    */
   public static final int LOG_USER = 1 << 3;

   /**
    * Mail system
    */
   public static final int LOG_MAIL = 2 << 3;

   /**
    * System daemons
    */
   public static final int LOG_DAEMON = 3 << 3;

   /**
    * security/authorization messages
    */
   public static final int LOG_AUTH = 4 << 3;

   /**
    * messages generated internally by syslogd
    */
   public static final int LOG_SYSLOG = 5 << 3;

   /**
    * line printer subsystem
    */
   public static final int LOG_LPR = 6 << 3;

   /**
    * network news subsystem
    */
   public static final int LOG_NEWS = 7 << 3;

   /**
    * UUCP subsystem
    */
   public static final int LOG_UUCP = 8 << 3;

   /**
    * clock daemon
    */
   public static final int LOG_CRON = 9 << 3;

   /**
    * security/authorization  messages (private)
    */
   public static final int LOG_AUTHPRIV = 10 << 3;

   /**
    * ftp daemon
    */
   public static final int LOG_FTP = 11 << 3;

   // other codes through 15 reserved for system use

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL0 = 16 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL1 = 17 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL2 = 18 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL3 = 19 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL4 = 20 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL5 = 21 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL6 = 22 << 3;

   /**
    * reserved for local use
    */
   public static final int LOG_LOCAL7 = 23 << 3;

   protected static final int SYSLOG_HOST_OI = 0;

   protected static final int FACILITY_OI = 1;

   static final String TAB = "    ";

   // Have LOG_USER as default
   int syslogFacility = LOG_USER;

   String facilityStr;

   boolean facilityPrinting = false;

   //SyslogTracerPrintWriter stp;
   SyslogQuietWriter sqw = null;

   String syslogHost = null;

   /**
    * If true, the appender will generate the HEADER (timestamp and host name) part of the syslog packet.
    *
    * @since 1.2.15
    */
   private boolean header = false;

   /**
    * Date format used if header = true.
    *
    * @since 1.2.15
    */
   private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss ", Locale.ENGLISH);

   /**
    * Host name used to identify messages from this appender.
    *
    * @since 1.2.15
    */
   private String localHostname = null;

   /**
    * Set to <code>true</code> after the header of the layout has been sent or if it has none.
    */
   private boolean layoutHeaderChecked = false;


   public SyslogAppender() {

      this.initSyslogFacilityStr();
   }


   public SyslogAppender(final Layout layout, final int syslogFacility) {

      this.layout = layout;
      this.syslogFacility = syslogFacility;
      this.initSyslogFacilityStr();
   }


   public SyslogAppender(final Layout layout, final String syslogHost, final int syslogFacility) {

      this(layout, syslogFacility);
      setSyslogHost(syslogHost);
   }


   /**
    * Release any resources held by this SyslogAppender.
    *
    * @since 0.8.4
    */
   public synchronized void close() {

      closed = true;
      if (sqw != null) {
         try {
            if (layoutHeaderChecked && layout != null && layout.getFooter() != null) {
               sendLayoutMessage(layout.getFooter());
            }
            sqw.close();
            sqw = null;
         } catch (final IOException ex) {
            sqw = null;
         }
      }
   }


   private void initSyslogFacilityStr() {

      facilityStr = getFacilityString(this.syslogFacility);

      if (facilityStr == null) {
         System.err.println("\"" + syslogFacility +
                 "\" is an unknown syslog facility. Defaulting to \"USER\".");
         this.syslogFacility = LOG_USER;
         facilityStr = "user:";
      } else {
         facilityStr += ":";
      }
   }


   /**
    * Returns the specified syslog facility as a lower-case String, e.g. "kern", "user", etc.
    */
   public
   static String getFacilityString(final int syslogFacility) {

      switch (syslogFacility) {
         case LOG_KERN:
            return "kern";
         case LOG_USER:
            return "user";
         case LOG_MAIL:
            return "mail";
         case LOG_DAEMON:
            return "daemon";
         case LOG_AUTH:
            return "auth";
         case LOG_SYSLOG:
            return "syslog";
         case LOG_LPR:
            return "lpr";
         case LOG_NEWS:
            return "news";
         case LOG_UUCP:
            return "uucp";
         case LOG_CRON:
            return "cron";
         case LOG_AUTHPRIV:
            return "authpriv";
         case LOG_FTP:
            return "ftp";
         case LOG_LOCAL0:
            return "local0";
         case LOG_LOCAL1:
            return "local1";
         case LOG_LOCAL2:
            return "local2";
         case LOG_LOCAL3:
            return "local3";
         case LOG_LOCAL4:
            return "local4";
         case LOG_LOCAL5:
            return "local5";
         case LOG_LOCAL6:
            return "local6";
         case LOG_LOCAL7:
            return "local7";
         default:
            return null;
      }
   }


   /**
    * Returns the integer value corresponding to the named syslog facility, or -1 if it couldn't be recognized.
    *
    * @param facilityName one of the strings KERN, USER, MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV,
    *                     FTP, LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7. The matching is
    *                     case-insensitive.
    * @since 1.1
    */
   public
   static int getFacility(String facilityName) {

      if (facilityName != null) {
         facilityName = facilityName.trim();
      }
      if ("KERN".equalsIgnoreCase(facilityName)) {
         return LOG_KERN;
      } else if ("USER".equalsIgnoreCase(facilityName)) {
         return LOG_USER;
      } else if ("MAIL".equalsIgnoreCase(facilityName)) {
         return LOG_MAIL;
      } else if ("DAEMON".equalsIgnoreCase(facilityName)) {
         return LOG_DAEMON;
      } else if ("AUTH".equalsIgnoreCase(facilityName)) {
         return LOG_AUTH;
      } else if ("SYSLOG".equalsIgnoreCase(facilityName)) {
         return LOG_SYSLOG;
      } else if ("LPR".equalsIgnoreCase(facilityName)) {
         return LOG_LPR;
      } else if ("NEWS".equalsIgnoreCase(facilityName)) {
         return LOG_NEWS;
      } else if ("UUCP".equalsIgnoreCase(facilityName)) {
         return LOG_UUCP;
      } else if ("CRON".equalsIgnoreCase(facilityName)) {
         return LOG_CRON;
      } else if ("AUTHPRIV".equalsIgnoreCase(facilityName)) {
         return LOG_AUTHPRIV;
      } else if ("FTP".equalsIgnoreCase(facilityName)) {
         return LOG_FTP;
      } else if ("LOCAL0".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL0;
      } else if ("LOCAL1".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL1;
      } else if ("LOCAL2".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL2;
      } else if ("LOCAL3".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL3;
      } else if ("LOCAL4".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL4;
      } else if ("LOCAL5".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL5;
      } else if ("LOCAL6".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL6;
      } else if ("LOCAL7".equalsIgnoreCase(facilityName)) {
         return LOG_LOCAL7;
      } else {
         return -1;
      }
   }


   private void splitPacket(final String header, final String packet) {

      final int byteCount = packet.getBytes().length;
      //
      //   if packet is less than RFC 3164 limit
      //      of 1024 bytes, then write it
      //      (must allow for up 5to 5 characters in the PRI section
      //          added by SyslogQuietWriter)
      if (byteCount <= 1019) {
         sqw.write(packet);
      } else {
         final int split = header.length() + (packet.length() - header.length() >> 1);
         splitPacket(header, packet.substring(0, split) + "...");
         splitPacket(header, header + "..." + packet.substring(split));
      }
   }


   public void append(final LoggingEvent event) {

      if (!isAsSevereAsThreshold(event.getLevel())) {
         return;
      }

      // We must not attempt to append if sqw is null.
      if (sqw == null) {
         errorHandler.error("No syslog host is set for SyslogAppedender named \"" +
                 this.name + "\".");
         return;
      }

      if (!layoutHeaderChecked) {
         if (layout != null && layout.getHeader() != null) {
            sendLayoutMessage(layout.getHeader());
         }
         layoutHeaderChecked = true;
      }

      final String hdr = getPacketHeader(event.timeStamp);
      String packet = layout.format(event);
      if (facilityPrinting || !hdr.isEmpty()) {
         final StringBuilder buf = new StringBuilder(hdr);
         if (facilityPrinting) {
            buf.append(facilityStr);
         }
         buf.append(packet);
         packet = buf.toString();
      }

      sqw.setLevel(event.getLevel().getSyslogEquivalent());
      //
      //   if message has a remote likelihood of exceeding 1024 bytes
      //      when encoded, consider splitting message into multiple packets
      if (packet.length() > 256) {
         splitPacket(hdr, packet);
      } else {
         sqw.write(packet);
      }

      if (layout.ignoresThrowable()) {
         final String[] s = event.getThrowableStrRep();
         if (s != null) {
            for (final String value : s) {
               if (value.startsWith("\t")) {
                  sqw.write(hdr + TAB + value.substring(1));
               } else {
                  sqw.write(hdr + value);
               }
            }
         }
      }
   }


   /**
    * This method returns immediately as options are activated when they are set.
    */
   public void activateOptions() {

      if (header) {
         getLocalHostname();
      }
      if (layout != null && layout.getHeader() != null) {
         sendLayoutMessage(layout.getHeader());
      }
      layoutHeaderChecked = true;
   }


   /**
    * The SyslogAppender requires a layout. Hence, this method returns <code>true</code>.
    *
    * @since 0.8.4
    */
   public boolean requiresLayout() {

      return true;
   }


   /**
    * The <b>SyslogHost</b> option is the name of the the syslog host where log output should go.  A non-default port
    * can be specified by appending a colon and port number to a host name, an IPv4 address or an IPv6 address enclosed
    * in square brackets.
    * <p/>
    * <b>WARNING</b> If the SyslogHost is not set, then this appender will fail.
    */
   public final void setSyslogHost(final String syslogHost) {

      this.sqw = new SyslogQuietWriter(new SyslogWriter(syslogHost),
              syslogFacility, errorHandler);
      //this.stp = new SyslogTracerPrintWriter(sqw);
      this.syslogHost = syslogHost;
   }


   /**
    * Returns the value of the <b>SyslogHost</b> option.
    */
   public String getSyslogHost() {

      return syslogHost;
   }


   /**
    * Set the syslog facility. This is the <b>Facility</b> option.
    * <p/>
    * <p>The <code>facilityName</code> parameter must be one of the strings KERN, USER, MAIL, DAEMON, AUTH, SYSLOG, LPR,
    * NEWS, UUCP, CRON, AUTHPRIV, FTP, LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7. Case is
    * unimportant.
    *
    * @since 0.8.1
    */
   public void setFacility(final String facilityName) {

      if (facilityName == null) {
         return;
      }

      syslogFacility = getFacility(facilityName);
      if (syslogFacility == -1) {
         System.err.println('[' + facilityName +
                 "] is an unknown syslog facility. Defaulting to [USER].");
         syslogFacility = LOG_USER;
      }

      this.initSyslogFacilityStr();

      // If there is already a sqw, make it use the new facility.
      if (sqw != null) {
         sqw.setSyslogFacility(this.syslogFacility);
      }
   }


   /**
    * Returns the value of the <b>Facility</b> option.
    */
   public String getFacility() {

      return getFacilityString(syslogFacility);
   }


   /**
    * If the <b>FacilityPrinting</b> option is set to true, the printed message will include the facility name of the
    * application. It is <em>false</em> by default.
    */
   public void setFacilityPrinting(final boolean on) {

      facilityPrinting = on;
   }


   /**
    * Returns the value of the <b>FacilityPrinting</b> option.
    */
   public boolean getFacilityPrinting() {

      return facilityPrinting;
   }


   /**
    * If true, the appender will generate the HEADER part (that is, timestamp and host name) of the syslog packet.
    * Default value is <code>false</code> for compatibility with existing behavior, however should be <code>true</code>
    * unless there is a specific justification.
    *
    * @since 1.2.15
    */
   public final boolean getHeader() {

      return header;
   }


   /**
    * Returns whether the appender produces the HEADER part (that is, timestamp and host name) of the syslog packet.
    *
    * @since 1.2.15
    */
   public final void setHeader(final boolean val) {

      header = val;
   }


   /**
    * Get the host name used to identify this appender.
    *
    * @return local host name
    * @since 1.2.15
    */
   private String getLocalHostname() {

      if (localHostname == null) {
         try {
            final InetAddress addr = InetAddress.getLocalHost();
            localHostname = addr.getHostName();
         } catch (final UnknownHostException uhe) {
            localHostname = "UNKNOWN_HOST";
         }
      }
      return localHostname;
   }


   /**
    * Gets HEADER portion of packet.
    *
    * @param timeStamp number of milliseconds after the standard base time.
    * @return HEADER portion of packet, will be zero-length string if header is false.
    * @since 1.2.15
    */
   private String getPacketHeader(final long timeStamp) {

      if (header) {
         final StringBuilder buf = new StringBuilder(dateFormat.format(new Date(timeStamp)));
         //  RFC 3164 says leading space, not leading zero on days 1-9
         if (buf.charAt(4) == '0') {
            buf.setCharAt(4, ' ');
         }
         buf.append(getLocalHostname());
         buf.append(' ');
         return buf.toString();
      }
      return "";
   }


   /**
    * Set header or footer of layout.
    *
    * @param msg message body, may not be null.
    */
   private void sendLayoutMessage(final String msg) {

      if (sqw != null) {
         String packet = msg;
         final String hdr = getPacketHeader(new Date().getTime());
         if (facilityPrinting || !hdr.isEmpty()) {
            final StringBuilder buf = new StringBuilder(hdr);
            if (facilityPrinting) {
               buf.append(facilityStr);
            }
            buf.append(msg);
            packet = buf.toString();
         }
         sqw.setLevel(6);
         sqw.write(packet);
      }
   }
}
