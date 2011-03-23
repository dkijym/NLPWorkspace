-- MySQL dump 9.09
--
-- Host: localhost    Database: emt03
---------------------------------------------------------
-- Server version	4.0.15-max-nt

--
-- Table structure for table `abook`
--

DROP TABLE IF EXISTS abook;
CREATE TABLE abook (
  groupid int(50) NOT NULL default '0',
  sender varchar(255) NOT NULL default '',
  PRIMARY KEY  (groupid,sender)
) TYPE=MyISAM;

--
-- Table structure for table `email`
--

DROP TABLE IF EXISTS email;
CREATE TABLE email (
  sender varchar(122) NOT NULL default '',
  sname varchar(32) NOT NULL default '',
  sdom varchar(32) NOT NULL default '',
  szn varchar(32) NOT NULL default '',
  rcpt varchar(122) NOT NULL default '',
  rname varchar(32) NOT NULL default '',
  rdom varchar(32) NOT NULL default '',
  rzn varchar(32) NOT NULL default '',
  numrcpt smallint(6) NOT NULL default '0',
  numattach smallint(6) default NULL,
  size int(10) unsigned default NULL,
  mailref varchar(255) NOT NULL default '',
  xmailer varchar(100) default '',
  dates date NOT NULL default '0000-00-00',
  times time NOT NULL default '00:00:00',
  flags int(10) unsigned default NULL,
  msghash varchar(32) NOT NULL default '',
  rplyto varchar(32) default '',
  forward varchar(5) default '',
  type varchar(32) NOT NULL default '',
  recnt int(10) unsigned default NULL,
  subject varchar(255) NOT NULL default '',
  timeadj int(10) unsigned default '0',
  folder varchar(40) default '',
  utime int(10) unsigned default '0',
  senderLoc char(1) default '',
  rcptLoc char(1) default '',
  insertTime datetime default NULL,
  recpath varchar(255) default '',
  UID int(10) unsigned NOT NULL auto_increment,
  class char(1) default '?',
  score double default '0',
  groups int(6) unsigned default '0',
  PRIMARY KEY  (msghash,sender,rcpt,dates,times),
  KEY insertTime (insertTime),
  KEY utime (utime),
  KEY UID (UID),
  KEY msghash (msghash),
  KEY mailref (mailref),
  KEY sender (sender)
) TYPE=MyISAM;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS groups;
CREATE TABLE groups (
  emails varchar(255) NOT NULL default '',
  groupid int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (groupid)
) TYPE=MyISAM;

--
-- Table structure for table `kwords`
--

DROP TABLE IF EXISTS kwords;
CREATE TABLE kwords (
  mailref varchar(255) NOT NULL default '',
  hotwords varchar(255) default NULL,
  PRIMARY KEY  (mailref)
) TYPE=MyISAM;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS message;
CREATE TABLE message (
  mailref varchar(255) NOT NULL default '',
  type varchar(30) default NULL,
  hash varchar(50) NOT NULL default '',
  received text NOT NULL,
  size int(10) unsigned default NULL,
  msghash varchar(255) NOT NULL default '',
  body mediumblob,
  filename varchar(255) default NULL,
  PRIMARY KEY  (mailref,hash),
  KEY hash (hash)
) TYPE=MyISAM;

--
-- Table structure for table `num`
--

DROP TABLE IF EXISTS num;
CREATE TABLE num (
  sender varchar(255) default NULL,
  cnt int(10) unsigned default NULL
) TYPE=MyISAM;

