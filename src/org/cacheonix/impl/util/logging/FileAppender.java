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

package org.cacheonix.impl.util.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;

import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.helpers.QuietWriter;
import org.cacheonix.impl.util.logging.spi.ErrorCode;

// Contributors: Jens Uwe Pipka <jens.pipka@gmx.de>
//              Ben Sandee

/**
 * FileAppender appends log events to a file.
 * <p/>
 * <p>Support for <code>java.io.Writer</code> and console appending has been deprecated and then removed. See the
 * replacement solutions: {@link WriterAppender} and {@link ConsoleAppender}.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public class FileAppender extends WriterAppender {

   /**
    * Controls file truncation. The default value for this variable is <code>true</code>, meaning that by default a
    * <code>FileAppender</code> will append to an existing file and not truncate it.
    * <p/>
    * <p>This option is meaningful only if the FileAppender opens the file.
    */
   protected boolean fileAppend = true;

   /**
    * The name of the log file.
    */
   protected String fileName = null;

   /**
    * Do we do bufferedIO?
    */
   protected boolean bufferedIO = false;

   /**
    * Determines the size of IO buffer be. Default is 8K.
    */
   protected int bufferSize = 8 << 10;


   /**
    * The default constructor does not do anything.
    */
   public FileAppender() {

   }


   /**
    * Instantiate a <code>FileAppender</code> and open the file designated by <code>filename</code>. The opened filename
    * will become the output destination for this appender.
    * <p/>
    * <p>If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file designated by
    * <code>filename</code> will be truncated before being opened.
    * <p/>
    * <p>If the <code>bufferedIO</code> parameter is <code>true</code>, then buffered IO will be used to write to the
    * output file.
    */
   public FileAppender(final Layout layout, final String filename, final boolean append,
                       final boolean bufferedIO,
                       final int bufferSize) throws IOException {

      this.layout = layout;
      this.setFile(filename, append, bufferedIO, bufferSize);
   }


   /**
    * Instantiate a FileAppender and open the file designated by <code>filename</code>. The opened filename will become
    * the output destination for this appender.
    * <p/>
    * <p>If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file designated by
    * <code>filename</code> will be truncated before being opened.
    */
   public FileAppender(final Layout layout, final String filename, final boolean append)
           throws IOException {

      this.layout = layout;
      this.setFile(filename, append, false, bufferSize);
   }


   /**
    * Instantiate a FileAppender and open the file designated by <code>filename</code>. The opened filename will become
    * the output destination for this appender.
    * <p/>
    * <p>The file will be appended to.
    */
   public FileAppender(final Layout layout, final String filename) throws IOException {

      this(layout, filename, true);
   }


   /**
    * The <b>File</b> property takes a string value which should be the name of the file to append to.
    * <p/>
    * <p><font color="#DD0044"><b>Note that the special values "System.out" or "System.err" are no longer
    * honored.</b></font>
    * <p/>
    * <p>Note: Actual opening of the file is made when {@link #activateOptions} is called, not when the options are
    * set.
    */
   public synchronized void setFile(final String file) {
      // Trim spaces from both ends. The users probably does not want
      // trailing spaces in file names.
      fileName = file.trim();
   }


   /**
    * Returns the value of the <b>Append</b> option.
    */
   public synchronized boolean getAppend() {

      return fileAppend;
   }


   /**
    * Returns the value of the <b>File</b> option.
    */
   public synchronized String getFile() {

      return fileName;
   }


   /**
    * If the value of <b>File</b> is not <code>null</code>, then {@link #setFile} is called with the values of
    * <b>File</b>  and <b>Append</b> properties.
    *
    * @since 0.8.1
    */
   public void activateOptions() {

      if (fileName != null) {
         try {
            setFile(fileName, fileAppend, bufferedIO, bufferSize);
         } catch (final IOException e) {
            errorHandler.error("setFile(" + fileName + ',' + fileAppend + ") call failed.",
                    e, ErrorCode.FILE_OPEN_FAILURE);
         }
      } else {
         //LogLog.error("File option not set for appender ["+name+"].");
         LogLog.warn("File option not set for appender [" + name + "].");
         LogLog.warn("Are you using FileAppender instead of ConsoleAppender?");
      }
   }


   /**
    * Closes the previously opened file.
    */
   protected final void closeFile() {

      if (this.qw != null) {
         try {
            this.qw.close();
         } catch (final IOException e) {
            // Exceptionally, it does not make sense to delegate to an
            // ErrorHandler. Since a closed appender is basically dead.
            //noinspection ObjectToString
            LogLog.error("Could not close " + qw, e);
         }
      }
   }


   /**
    * Get the value of the <b>BufferedIO</b> option.
    * <p/>
    * <p>BufferedIO will significantly increase performance on heavily loaded systems.
    */
   public synchronized boolean getBufferedIO() {

      return this.bufferedIO;
   }


   /**
    * Get the size of the IO buffer.
    */
   public synchronized int getBufferSize() {

      return this.bufferSize;
   }


   /**
    * The <b>Append</b> option takes a boolean value. It is set to <code>true</code> by default. If true, then
    * <code>File</code> will be opened in append mode by {@link #setFile setFile} (see above). Otherwise, {@link
    * #setFile setFile} will open <code>File</code> in truncate mode.
    * <p/>
    * <p>Note: Actual opening of the file is made when {@link #activateOptions} is called, not when the options are
    * set.
    */
   public synchronized void setAppend(final boolean flag) {

      fileAppend = flag;
   }


   /**
    * The <b>BufferedIO</b> option takes a boolean value. It is set to <code>false</code> by default. If true, then
    * <code>File</code> will be opened and the resulting {@link Writer} wrapped around a {@link BufferedWriter}.
    * <p/>
    * BufferedIO will significantly increase performance on heavily loaded systems.
    */
   public synchronized void setBufferedIO(final boolean bufferedIO) {

      this.bufferedIO = bufferedIO;
      if (bufferedIO) {
         immediateFlush = false;
      }
   }


   /**
    * Set the size of the IO buffer.
    */
   public synchronized void setBufferSize(final int bufferSize) {

      this.bufferSize = bufferSize;
   }


   /**
    * <p>Sets and <i>opens</i> the file where the log output will go. The specified file must be writable.
    * <p/>
    * <p>If there was already an opened file, then the previous file is closed first.
    * <p/>
    * <p><b>Do not use this method directly. To configure a FileAppender or one of its subclasses, set its properties
    * one by one and then call activateOptions.</b>
    *
    * @param fileName The path to the log file.
    * @param append   If <code>true</code> will append to fileName. Otherwise will truncate fileName.
    */
   public
   synchronized void setFile(final String fileName, final boolean append, final boolean bufferedIO,
                             final int bufferSize)
           throws IOException {

      LogLog.debug("setFile called: " + fileName + ", " + append);

      // It does not make sense to have immediate flush and bufferedIO.
      if (bufferedIO) {
         setImmediateFlush(false);
      }

      reset();
      FileOutputStream ostream = null;
      try {
         //
         //   attempt to create file
         //
         ostream = new FileOutputStream(fileName, append);
      } catch (final FileNotFoundException ex) {
         //
         //   if parent directory does not exist then
         //      attempt to create it and try to create file
         //      see bug 9150
         //
         final String parentName = new File(fileName).getParent();
         if (parentName != null) {
            final File parentDir = new File(parentName);
            if (!parentDir.exists() && parentDir.mkdirs()) {
               ostream = new FileOutputStream(fileName, append);
            } else {
               throw ex;
            }
         } else {
            throw ex;
         }
      }
      Writer fw = createWriter(ostream);
      if (bufferedIO) {
         fw = new BufferedWriter(fw, bufferSize);
      }
      this.setQWForFiles(fw);
      this.fileName = fileName;
      this.fileAppend = append;
      this.bufferedIO = bufferedIO;
      this.bufferSize = bufferSize;
      writeHeader();
      LogLog.debug("setFile ended");
   }


   /**
    * Sets the quiet writer being used.
    * <p/>
    * This method is overridden by {@link RollingFileAppender}.
    */
   protected void setQWForFiles(final Writer writer) {

      this.qw = new QuietWriter(writer, errorHandler);
   }


   /**
    * Close any previously opened file and call the parent's <code>reset</code>.
    */
   protected final void reset() {

      closeFile();
      this.fileName = null;
      super.reset();
   }
}

