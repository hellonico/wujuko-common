(ns wujuko-common.zio
  (:require [clojure.java.io :as io])
  (:import [java.io FileOutputStream PrintWriter SequenceInputStream]
           [org.apache.commons.compress.compressors.gzip
            GzipCompressorInputStream GzipCompressorOutputStream]
           [org.apache.commons.compress.compressors.bzip2
            BZip2CompressorInputStream BZip2CompressorOutputStream]
           [org.apache.commons.io FileUtils]))

;;
;; Use zreader and zwriter to obtain a reader or writer that is
;; compressed if the provided filename ends in .bz2 or .gz
;;

(defn- reader
  "Fallback default reader if the stream isn't compressed."
  [filename]
  (-> filename io/reader))

;;
;; Pass 'true' to GzipCompressorInputStream to tell it to handle
;; multipart gzip files.
(defn- gzip-reader
  "Correctly handles multi-member gzip files, default java gzip does not."
  [filename]
  (-> filename io/input-stream (GzipCompressorInputStream. true) io/reader))

(defn- bzip2-reader
  "Reader for bzip2 files."
  [filename]
  (-> filename io/input-stream BZip2CompressorInputStream. io/reader))

(defn- writer
  "Default writer if compressed output is not desired."
  [filename]
  (-> filename io/file io/output-stream io/writer))

(defn- gzip-writer
  "Writer for gzip files."
  [filename]
  (-> filename io/file io/output-stream GzipCompressorOutputStream. io/writer))

(defn- bzip2-writer
  "Writer for bzip2 files."
  [filename]
  (-> filename io/file io/output-stream BZip2CompressorOutputStream. io/writer))

(defn zreader
  "Naive reader dispatch using file extension to determine file type."
  [filename]
  (let [fn (if (= java.net.URL (type filename)) (.getFile filename) filename)]
    (cond
      (.endsWith fn ".gz") (gzip-reader filename)
      (.endsWith fn ".bz2") (bzip2-reader filename)
      :else (reader filename))))

(defn zwriter
  "Naive writer dispatch using file extension to determine file type."
  [filename]
  (cond
    (.endsWith filename ".gz") (gzip-writer filename)
    (.endsWith filename ".bz2") (bzip2-writer filename)
    :else (writer filename)))

;;
;; Multiple files as Single Seq Stream

(defn coll->enumeration
  "Create an Enumeration from the provided collection."
  [coll]
  (clojure.lang.SeqEnumeration. coll))

(defn wildcard-filter
  "Given a regex, return a FilenameFilter that matches."
  [re]
  (reify java.io.FilenameFilter
    (accept [_ dir name] (not (nil? (re-find re name))))))

(defn directory-list
  "Given a directory and a regex, return a sorted seq of matching filenames."
  [dir re]
  (sort (.list (clojure.java.io/file dir) (wildcard-filter re))))

(defn match-multi-input-stream
  "Find the files in *dir* that match the regular expression *regx* and return
   a sequence of input streams."
  [dir regx]
  (map #(io/input-stream (str dir %)) (directory-list dir regx)))

(defn matching-files-reader
  "Given a directory and a regular expression, provides a reader that
   treats all matching files as a single input stream.

   (with-open [rdr (matching-files-reader \"/files/\" #\"*\\.txt\")]
       ;; do something
     )
  "
  [dir filere]
  (io/reader
   (SequenceInputStream.
    (coll->enumeration (match-multi-input-stream dir filere)))))

(defn move-file
  "Uses apache commons io to move file. Exceptions thrown during fail."
  [src dest]
  (FileUtils/moveFile (io/file src) (io/file dest)))

(defn move-to-dir
  "Uses apache commons io to move file. Exceptions thrown during fail."
  [src destdir]
  (FileUtils/moveToDirectory (io/file src) (io/file destdir) false))
