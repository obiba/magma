-- phpMyAdmin SQL Dump
-- version 3.5.0-rc2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 22, 2012 at 11:34 AM
-- Server version: 5.1.61-0ubuntu0.11.04.1
-- PHP Version: 5.3.5-1ubuntu7.7

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `limesurvey_test`
--

-- --------------------------------------------------------

--
-- Table structure for table `answers`
--

CREATE TABLE IF NOT EXISTS `answers` (
  `qid` int(11) NOT NULL DEFAULT '0',
  `code` varchar(5) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `answer` text COLLATE utf8_unicode_ci NOT NULL,
  `assessment_value` int(11) NOT NULL DEFAULT '0',
  `sortorder` int(11) NOT NULL,
  `language` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `scale_id` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`qid`,`code`,`language`,`scale_id`),
  KEY `answers_idx2` (`sortorder`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `answers`
--

INSERT INTO `answers` (`qid`, `code`, `answer`, `assessment_value`, `sortorder`, `language`, `scale_id`) VALUES
(1, 'A3', 'category 3', 1, 3, 'en', 0),
(1, 'A2', 'catégorie 2', 1, 2, 'fr', 0),
(1, 'A2', 'category 2', 1, 2, 'en', 0),
(1, 'A1', 'catégorie 1', 0, 1, 'fr', 0),
(1, 'A1', 'category 1', 0, 1, 'en', 0),
(1, 'A3', 'catégorie 3', 1, 3, 'fr', 0),
(7, 'A1', 'option 2-1', 0, 1, 'en', 1),
(7, 'A2', 'option 1-2', 1, 2, 'en', 0),
(7, 'A2', 'option 1-2', 1, 2, 'fr', 0),
(7, 'A1', 'option 1-1', 0, 1, 'en', 0),
(7, 'A1', 'option 1-1', 0, 1, 'fr', 0),
(7, 'A1', 'option 2-1', 0, 1, 'fr', 1),
(7, 'A2', 'option 2-2', 1, 2, 'en', 1),
(7, 'A2', 'option 2-2', 1, 2, 'fr', 1),
(10, 'A1', 'option 1', 0, 1, 'fr', 0),
(10, 'A1', 'option 1', 0, 1, 'en', 0),
(10, 'A2', 'option 2', 1, 2, 'en', 0),
(10, 'A2', 'option 2', 1, 2, 'fr', 0),
(12, 'A1', 'option 1', 0, 1, 'fr', 0),
(12, 'A1', 'option 1', 0, 1, 'en', 0),
(12, 'A2', 'option 2', 1, 2, 'en', 0),
(12, 'A2', 'option 2', 1, 2, 'fr', 0),
(15, 'A1', 'Some example answer option', 0, 1, 'en', 0),
(15, 'A1', 'Some example answer option', 0, 1, 'fr', 0),
(17, 'A1', 'Some example answer option', 0, 1, 'en', 0),
(17, 'A1', 'Some example answer option', 0, 1, 'fr', 0);

-- --------------------------------------------------------

--
-- Table structure for table `assessments`
--

CREATE TABLE IF NOT EXISTS `assessments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sid` int(11) NOT NULL DEFAULT '0',
  `scope` varchar(5) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `gid` int(11) NOT NULL DEFAULT '0',
  `name` text COLLATE utf8_unicode_ci NOT NULL,
  `minimum` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `maximum` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `message` text COLLATE utf8_unicode_ci NOT NULL,
  `language` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  PRIMARY KEY (`id`,`language`),
  KEY `assessments_idx2` (`sid`),
  KEY `assessments_idx3` (`gid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `conditions`
--

CREATE TABLE IF NOT EXISTS `conditions` (
  `cid` int(11) NOT NULL AUTO_INCREMENT,
  `qid` int(11) NOT NULL DEFAULT '0',
  `scenario` int(11) NOT NULL DEFAULT '1',
  `cqid` int(11) NOT NULL DEFAULT '0',
  `cfieldname` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `method` char(5) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `value` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`cid`),
  KEY `conditions_idx2` (`qid`),
  KEY `conditions_idx3` (`cqid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `defaultvalues`
--

CREATE TABLE IF NOT EXISTS `defaultvalues` (
  `qid` int(11) NOT NULL DEFAULT '0',
  `specialtype` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `scale_id` int(11) NOT NULL DEFAULT '0',
  `sqid` int(11) NOT NULL DEFAULT '0',
  `language` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `defaultvalue` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`qid`,`scale_id`,`language`,`specialtype`,`sqid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `expression_errors`
--

CREATE TABLE IF NOT EXISTS `expression_errors` (
  `id` int(9) NOT NULL AUTO_INCREMENT,
  `errortime` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `sid` int(11) DEFAULT NULL,
  `gid` int(11) DEFAULT NULL,
  `qid` int(11) DEFAULT NULL,
  `gseq` int(11) DEFAULT NULL,
  `qseq` int(11) DEFAULT NULL,
  `type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `eqn` text COLLATE utf8_unicode_ci,
  `prettyprint` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `failed_login_attempts`
--

CREATE TABLE IF NOT EXISTS `failed_login_attempts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(37) COLLATE utf8_unicode_ci NOT NULL,
  `last_attempt` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `number_attempts` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `groups`
--

CREATE TABLE IF NOT EXISTS `groups` (
  `gid` int(11) NOT NULL AUTO_INCREMENT,
  `sid` int(11) NOT NULL DEFAULT '0',
  `group_name` varchar(100) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `group_order` int(11) NOT NULL DEFAULT '0',
  `description` text COLLATE utf8_unicode_ci,
  `language` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `randomization_group` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `grelevance` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`gid`,`language`),
  KEY `groups_idx2` (`sid`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=3 ;

--
-- Dumping data for table `groups`
--

INSERT INTO `groups` (`gid`, `sid`, `group_name`, `group_order`, `description`, `language`, `randomization_group`, `grelevance`) VALUES
(1, 65284, 'QuestionGroup1', 0, 'sdfsdf', 'en', '', NULL),
(1, 65284, 'Groupe de questions 1', 0, 'sdfsdfbnbnm', 'fr', '', NULL),
(2, 65284, 'Groupe de questions 2', 1, 'sdfsdf', 'fr', '', NULL),
(2, 65284, 'QuestionGroup2', 1, 'sdfsdf', 'en', '', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `labels`
--

CREATE TABLE IF NOT EXISTS `labels` (
  `lid` int(11) NOT NULL DEFAULT '0',
  `code` varchar(5) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `title` text COLLATE utf8_unicode_ci,
  `sortorder` int(11) NOT NULL,
  `assessment_value` int(11) NOT NULL DEFAULT '0',
  `language` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  PRIMARY KEY (`lid`,`sortorder`,`language`),
  KEY `ixcode` (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `labelsets`
--

CREATE TABLE IF NOT EXISTS `labelsets` (
  `lid` int(11) NOT NULL AUTO_INCREMENT,
  `label_name` varchar(100) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `languages` varchar(200) COLLATE utf8_unicode_ci DEFAULT 'en',
  PRIMARY KEY (`lid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `participants`
--

CREATE TABLE IF NOT EXISTS `participants` (
  `participant_id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `firstname` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastname` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(80) COLLATE utf8_unicode_ci DEFAULT NULL,
  `language` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `blacklisted` varchar(1) COLLATE utf8_unicode_ci NOT NULL,
  `owner_uid` int(20) NOT NULL,
  PRIMARY KEY (`participant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `participant_attribute`
--

CREATE TABLE IF NOT EXISTS `participant_attribute` (
  `participant_id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `attribute_id` int(11) NOT NULL,
  `value` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`participant_id`,`attribute_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `participant_attribute_names`
--

CREATE TABLE IF NOT EXISTS `participant_attribute_names` (
  `attribute_id` int(11) NOT NULL AUTO_INCREMENT,
  `attribute_type` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  `visible` char(5) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`attribute_id`,`attribute_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `participant_attribute_names_lang`
--

CREATE TABLE IF NOT EXISTS `participant_attribute_names_lang` (
  `attribute_id` int(11) NOT NULL,
  `attribute_name` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `lang` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`attribute_id`,`lang`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `participant_attribute_values`
--

CREATE TABLE IF NOT EXISTS `participant_attribute_values` (
  `value_id` int(11) NOT NULL AUTO_INCREMENT,
  `attribute_id` int(11) NOT NULL,
  `value` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `participant_shares`
--

CREATE TABLE IF NOT EXISTS `participant_shares` (
  `participant_id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `share_uid` int(11) NOT NULL,
  `date_added` datetime NOT NULL,
  `can_edit` varchar(5) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`participant_id`,`share_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `questions`
--

CREATE TABLE IF NOT EXISTS `questions` (
  `qid` int(11) NOT NULL AUTO_INCREMENT,
  `parent_qid` int(11) NOT NULL DEFAULT '0',
  `sid` int(11) NOT NULL DEFAULT '0',
  `gid` int(11) NOT NULL DEFAULT '0',
  `type` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'T',
  `title` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `question` text COLLATE utf8_unicode_ci NOT NULL,
  `preg` text COLLATE utf8_unicode_ci,
  `help` text COLLATE utf8_unicode_ci,
  `other` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'N',
  `mandatory` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `question_order` int(11) NOT NULL,
  `language` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `scale_id` tinyint(4) NOT NULL DEFAULT '0',
  `same_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Saves if user set to use the same default value across languages in default options dialog',
  `relevance` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`qid`,`language`),
  KEY `questions_idx2` (`sid`),
  KEY `questions_idx3` (`gid`),
  KEY `questions_idx4` (`type`),
  KEY `parent_qid_idx` (`parent_qid`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=40 ;

--
-- Dumping data for table `questions`
--

INSERT INTO `questions` (`qid`, `parent_qid`, `sid`, `gid`, `type`, `title`, `question`, `preg`, `help`, `other`, `mandatory`, `question_order`, `language`, `scale_id`, `same_default`, `relevance`) VALUES
(1, 0, 65284, 1, 'L', 'Q1', 'List radio', '', 'help, i need somebody', 'Y', 'Y', 0, 'en', 0, 0, NULL),
(1, 0, 65284, 1, 'L', 'Q1', 'List radio', '', 'aide', 'Y', 'Y', 0, 'fr', 0, 0, NULL),
(2, 0, 65284, 1, 'T', 'Q2', 'Long free text', '', 'sdfsdf', 'N', 'N', 1, 'en', 0, 0, NULL),
(2, 0, 65284, 1, 'T', 'Q2', 'Long free text', '', '', 'N', 'N', 1, 'fr', 0, 0, NULL),
(3, 0, 65284, 2, 'A', 'Q3', 'question label 3', '', '', 'N', 'Y', 0, 'en', 0, 0, NULL),
(3, 0, 65284, 2, 'A', 'Q3', 'label de la question 3', '', '', 'N', 'Y', 0, 'fr', 0, 0, NULL),
(4, 0, 65284, 2, '|', 'Q4', 'File upload', '', '', 'N', 'N', 14, 'en', 0, 0, NULL),
(4, 0, 65284, 2, '|', 'Q4', 'File upload', '', '', 'N', 'N', 14, 'fr', 0, 0, NULL),
(5, 0, 65284, 2, ';', 'Q5', 'Array Texts', '', '', 'N', 'N', 15, 'en', 0, 0, NULL),
(5, 0, 65284, 2, ';', 'Q5', 'Array Texts', '', '', 'N', 'N', 15, 'fr', 0, 0, NULL),
(6, 0, 65284, 2, ':', 'Q6', 'Array Numbers', '', '', 'N', 'N', 16, 'en', 0, 0, NULL),
(6, 0, 65284, 2, ':', 'Q6', 'Array Numbers', '', '', 'N', 'N', 16, 'fr', 0, 0, NULL),
(7, 0, 65284, 2, '1', 'Q7', 'Array dual scale', '', '', 'N', 'N', 17, 'en', 0, 0, NULL),
(7, 0, 65284, 2, '1', 'Q7', 'Array dual scale', '', '', 'N', 'N', 17, 'fr', 0, 0, NULL),
(8, 0, 65284, 1, 'M', 'Q8', 'Multiple choice', '', '', 'N', 'N', 7, 'en', 0, 0, NULL),
(8, 0, 65284, 1, 'M', 'Q8', 'Multiple choice', '', '', 'N', 'N', 7, 'fr', 0, 0, NULL),
(9, 0, 65284, 1, 'P', 'Q9', 'Multiple choice with comments', '', '', 'N', 'N', 10, 'en', 0, 0, NULL),
(9, 0, 65284, 1, 'P', 'Q9', 'Multiple choice with comments', '', '', 'N', 'N', 10, 'fr', 0, 0, NULL),
(10, 0, 65284, 1, 'O', 'Q10', 'List with comment', '', '', 'N', 'N', 11, 'en', 0, 0, NULL),
(10, 0, 65284, 1, 'O', 'Q10', 'List with comment', '', '', 'N', 'N', 11, 'fr', 0, 0, NULL),
(11, 0, 65284, 1, 'K', 'Q11', 'Multiple numerical input', '', '', 'N', 'N', 12, 'en', 0, 0, NULL),
(11, 0, 65284, 1, 'K', 'Q11', 'Multiple numerical input', '', '', 'N', 'N', 12, 'fr', 0, 0, NULL),
(12, 0, 65284, 1, 'R', 'Q12', 'Ranking', '', '', 'N', 'N', 13, 'en', 0, 0, NULL),
(12, 0, 65284, 1, 'R', 'Q12', 'Ranking', '', '', 'N', 'N', 13, 'fr', 0, 0, NULL),
(13, 0, 65284, 1, 'Y', 'Q13', 'Yes/No', '', '', 'N', 'Y', 14, 'en', 0, 0, NULL),
(13, 0, 65284, 1, 'Y', 'Q13', 'Yes/No', '', '', 'N', 'Y', 14, 'fr', 0, 0, NULL),
(14, 0, 65284, 2, 'E', 'Q14', 'Array increase same decrease', '', '', 'N', 'N', 18, 'en', 0, 0, NULL),
(14, 0, 65284, 2, 'E', 'Q14', 'Array increase same decrease', '', '', 'N', 'N', 18, 'fr', 0, 0, NULL),
(15, 0, 65284, 2, 'H', 'Q15', 'Array by column', '', '', 'N', 'N', 19, 'en', 0, 0, NULL),
(15, 0, 65284, 2, 'H', 'Q15', 'Array by column', '', '', 'N', 'N', 19, 'fr', 0, 0, NULL),
(16, 0, 65284, 1, 'G', 'Q16', 'Gender', '', '', 'N', 'N', 15, 'en', 0, 0, NULL),
(16, 0, 65284, 1, 'G', 'Q16', 'Gendre', '', '', 'N', 'N', 15, 'fr', 0, 0, NULL),
(17, 0, 65284, 1, '!', 'Q17', 'List dropdown', '', '', 'Y', 'N', 16, 'en', 0, 0, NULL),
(17, 0, 65284, 1, '!', 'Q17', 'List dropdown', '', '', 'Y', 'N', 16, 'fr', 0, 0, NULL),
(18, 0, 65284, 1, 'D', 'Q18', 'Date', '', '', 'N', 'N', 17, 'en', 0, 0, NULL),
(18, 0, 65284, 1, 'D', 'Q18', 'Date', '', '', 'N', 'N', 17, 'fr', 0, 0, NULL),
(19, 3, 65284, 2, 'T', 'SQ001', 'subquestion 1', '', '', 'N', '', 12, 'en', 0, 0, NULL),
(19, 3, 65284, 2, 'T', 'SQ001', 'sous question 1', '', '', 'N', '', 12, 'fr', 0, 0, NULL),
(20, 3, 65284, 2, 'T', 'SQ002', 'subquestion 2', '', '', 'N', '', 13, 'en', 0, 0, NULL),
(20, 3, 65284, 2, 'T', 'SQ002', 'sous question 2', '', '', 'N', '', 13, 'fr', 0, 0, NULL),
(21, 5, 65284, 2, 'T', 'SQY1', 'subquestion y1', '', '', 'N', '', 6, 'en', 0, 0, NULL),
(21, 5, 65284, 2, 'T', 'SQY1', 'sous question y1', '', '', 'N', '', 6, 'fr', 0, 0, NULL),
(22, 5, 65284, 2, 'T', 'SQX1', 'subquestion x1', '', '', 'N', '', 4, 'en', 1, 0, NULL),
(22, 5, 65284, 2, 'T', 'SQX1', 'sous question x1', '', '', 'N', '', 4, 'fr', 1, 0, NULL),
(23, 5, 65284, 2, 'T', 'SQY2', 'subquestion y2', '', '', 'N', '', 11, 'en', 0, 0, NULL),
(23, 5, 65284, 2, 'T', 'SQY2', 'sous question y2', '', '', 'N', '', 11, 'fr', 0, 0, NULL),
(24, 5, 65284, 2, 'T', 'SQX2', 'subquestion x2', '', '', 'N', '', 8, 'en', 1, 0, NULL),
(24, 5, 65284, 2, 'T', 'SQX2', 'sous question x2', '', '', 'N', '', 8, 'fr', 1, 0, NULL),
(25, 6, 65284, 2, 'T', 'SQY1', 'subquestion y1', '', '', 'N', '', 5, 'en', 0, 0, NULL),
(25, 6, 65284, 2, 'T', 'SQY1', 'sous question y1', '', '', 'N', '', 5, 'fr', 0, 0, NULL),
(26, 6, 65284, 2, 'T', 'SQX1', 'subquestion x1', '', '', 'N', '', 3, 'en', 1, 0, NULL),
(26, 6, 65284, 2, 'T', 'SQX1', 'sous question x1', '', '', 'N', '', 3, 'fr', 1, 0, NULL),
(27, 7, 65284, 2, 'T', 'SQ001', 'subquestion 1', '', '', 'N', '', 2, 'en', 0, 0, NULL),
(27, 7, 65284, 2, 'T', 'SQ001', 'sous question 1', '', '', 'N', '', 2, 'fr', 0, 0, NULL),
(28, 6, 65284, 2, 'T', 'SQY2', 'subquestion y2', '', '', 'N', '', 10, 'en', 0, 0, NULL),
(28, 6, 65284, 2, 'T', 'SQY2', 'sous question y2', '', '', 'N', '', 10, 'fr', 0, 0, NULL),
(29, 6, 65284, 2, 'T', 'SQX2', 'subquestion x2', '', '', 'N', '', 9, 'en', 1, 0, NULL),
(29, 6, 65284, 2, 'T', 'SQX2', 'sous question x2', '', '', 'N', '', 9, 'fr', 1, 0, NULL),
(30, 7, 65284, 2, 'T', 'SQ002', 'subquestion 2', '', '', 'N', '', 7, 'en', 0, 0, NULL),
(30, 7, 65284, 2, 'T', 'SQ002', 'sous question 2', '', '', 'N', '', 7, 'fr', 0, 0, NULL),
(31, 8, 65284, 1, 'T', 'SQ001', 'choice 1', '', '', 'N', '', 5, 'en', 0, 0, NULL),
(31, 8, 65284, 1, 'T', 'SQ001', 'choix 1', '', '', 'N', '', 5, 'fr', 0, 0, NULL),
(32, 8, 65284, 1, 'T', 'SQ002', 'choice 2', '', '', 'N', '', 8, 'en', 0, 0, NULL),
(32, 8, 65284, 1, 'T', 'SQ002', 'choix 2', '', '', 'N', '', 8, 'fr', 0, 0, NULL),
(33, 8, 65284, 1, 'T', 'SQ003', 'choice 3', '', '', 'N', '', 9, 'en', 0, 0, NULL),
(33, 8, 65284, 1, 'T', 'SQ003', 'choix 3', '', '', 'N', '', 9, 'fr', 0, 0, NULL),
(34, 9, 65284, 1, 'T', 'SQ001', 'choice 1', '', '', 'N', '', 3, 'en', 0, 0, NULL),
(34, 9, 65284, 1, 'T', 'SQ001', 'choix 1', '', '', 'N', '', 3, 'fr', 0, 0, NULL),
(35, 9, 65284, 1, 'T', 'SQ002', 'choice 2', '', '', 'N', '', 6, 'en', 0, 0, NULL),
(35, 9, 65284, 1, 'T', 'SQ002', 'choix 2', '', '', 'N', '', 6, 'fr', 0, 0, NULL),
(36, 11, 65284, 1, 'T', 'SQ001', 'subquestion 1', '', '', 'N', '', 2, 'en', 0, 0, NULL),
(36, 11, 65284, 1, 'T', 'SQ001', 'sous question 1', '', '', 'N', '', 2, 'fr', 0, 0, NULL),
(37, 11, 65284, 1, 'T', 'SQ002', 'subquestion 2', '', '', 'N', '', 4, 'en', 0, 0, NULL),
(37, 11, 65284, 1, 'T', 'SQ002', 'sous question 2', '', '', 'N', '', 4, 'fr', 0, 0, NULL),
(38, 14, 65284, 2, 'T', 'SQ001', 'Some example subquestion', '', '', 'N', '', 1, 'en', 0, 0, NULL),
(38, 14, 65284, 2, 'T', 'SQ001', 'Some example subquestion', '', '', 'N', '', 1, 'fr', 0, 0, NULL),
(39, 15, 65284, 2, 'T', 'SQ001', 'Some example subquestion', '', '', 'N', '', 1, 'en', 0, 0, NULL),
(39, 15, 65284, 2, 'T', 'SQ001', 'Some example subquestion', '', '', 'N', '', 1, 'fr', 0, 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `question_attributes`
--

CREATE TABLE IF NOT EXISTS `question_attributes` (
  `qaid` int(11) NOT NULL AUTO_INCREMENT,
  `qid` int(11) NOT NULL DEFAULT '0',
  `attribute` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `value` text COLLATE utf8_unicode_ci,
  `language` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`qaid`),
  KEY `question_attributes_idx2` (`qid`),
  KEY `question_attributes_idx3` (`attribute`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=212 ;

--
-- Dumping data for table `question_attributes`
--

INSERT INTO `question_attributes` (`qaid`, `qid`, `attribute`, `value`, `language`) VALUES
(1, 1, 'alphasort', '0', NULL),
(2, 1, 'array_filter', '', NULL),
(3, 1, 'array_filter_exclude', '', NULL),
(4, 1, 'display_columns', '1', NULL),
(5, 1, 'hidden', '0', NULL),
(6, 1, 'hide_tip', '0', NULL),
(7, 1, 'other_comment_mandatory', '1', NULL),
(8, 1, 'other_numbers_only', '0', NULL),
(9, 1, 'other_replace_text', '', NULL),
(10, 1, 'page_break', '0', NULL),
(11, 1, 'public_statistics', '0', NULL),
(12, 1, 'random_group', '', NULL),
(13, 1, 'random_order', '0', NULL),
(14, 1, 'scale_export', '0', NULL),
(15, 2, 'display_rows', '', NULL),
(16, 2, 'hidden', '0', NULL),
(17, 2, 'maximum_chars', '', NULL),
(18, 2, 'page_break', '0', NULL),
(19, 2, 'random_group', '', NULL),
(20, 2, 'text_input_width', '', NULL),
(21, 2, 'time_limit', '', NULL),
(22, 2, 'time_limit_action', '1', NULL),
(23, 2, 'time_limit_countdown_message', '', NULL),
(24, 2, 'time_limit_disable_next', '0', NULL),
(25, 2, 'time_limit_disable_prev', '0', NULL),
(26, 2, 'time_limit_message', '', NULL),
(27, 2, 'time_limit_message_delay', '', NULL),
(28, 2, 'time_limit_message_style', '', NULL),
(29, 2, 'time_limit_timer_style', '', NULL),
(30, 2, 'time_limit_warning', '', NULL),
(31, 2, 'time_limit_warning_2', '', NULL),
(32, 2, 'time_limit_warning_2_display_time', '', NULL),
(33, 2, 'time_limit_warning_2_message', '', NULL),
(34, 2, 'time_limit_warning_2_style', '', NULL),
(35, 2, 'time_limit_warning_display_time', '', NULL),
(36, 2, 'time_limit_warning_message', '', NULL),
(37, 2, 'time_limit_warning_style', '', NULL),
(38, 3, 'answer_width', '', NULL),
(39, 3, 'array_filter', '', NULL),
(40, 3, 'array_filter_exclude', '', NULL),
(41, 3, 'hidden', '0', NULL),
(42, 3, 'page_break', '0', NULL),
(43, 3, 'public_statistics', '0', NULL),
(44, 3, 'random_group', '', NULL),
(45, 3, 'random_order', '0', NULL),
(46, 4, 'allowed_filetypes', 'png, gif, doc, odt', NULL),
(47, 4, 'hidden', '0', NULL),
(48, 4, 'max_filesize', '1024', NULL),
(49, 4, 'max_num_of_files', '1', NULL),
(50, 4, 'min_num_of_files', '0', NULL),
(51, 4, 'page_break', '0', NULL),
(52, 4, 'random_group', '', NULL),
(53, 4, 'show_comment', '1', NULL),
(54, 4, 'show_title', '1', NULL),
(55, 5, 'answer_width', '', NULL),
(56, 5, 'array_filter', '', NULL),
(57, 5, 'array_filter_exclude', '', NULL),
(58, 5, 'hidden', '0', NULL),
(59, 5, 'numbers_only', '0', NULL),
(60, 5, 'page_break', '0', NULL),
(61, 5, 'random_group', '', NULL),
(62, 5, 'random_order', '0', NULL),
(63, 5, 'show_grand_total', '0', NULL),
(64, 5, 'show_totals', 'X', NULL),
(65, 5, 'text_input_width', '', NULL),
(66, 6, 'answer_width', '', NULL),
(67, 6, 'array_filter', '', NULL),
(68, 6, 'array_filter_exclude', '', NULL),
(69, 6, 'hidden', '0', NULL),
(70, 6, 'input_boxes', '0', NULL),
(71, 6, 'maximum_chars', '', NULL),
(72, 6, 'multiflexible_checkbox', '0', NULL),
(73, 6, 'multiflexible_max', '333', NULL),
(74, 6, 'multiflexible_min', '222', NULL),
(75, 6, 'multiflexible_step', '10', NULL),
(76, 6, 'page_break', '0', NULL),
(77, 6, 'public_statistics', '0', NULL),
(78, 6, 'random_group', '', NULL),
(79, 6, 'random_order', '0', NULL),
(80, 6, 'reverse', '0', NULL),
(81, 6, 'scale_export', '0', NULL),
(82, 7, 'answer_width', '', NULL),
(83, 7, 'array_filter', '', NULL),
(84, 7, 'array_filter_exclude', '', NULL),
(85, 7, 'dropdown_prepostfix', '', NULL),
(86, 7, 'dropdown_separators', '', NULL),
(87, 7, 'dualscale_headerA', '', NULL),
(88, 7, 'dualscale_headerB', '', NULL),
(89, 7, 'hidden', '0', NULL),
(90, 7, 'page_break', '0', NULL),
(91, 7, 'public_statistics', '0', NULL),
(92, 7, 'random_group', '', NULL),
(93, 7, 'random_order', '0', NULL),
(94, 7, 'scale_export', '0', NULL),
(95, 7, 'use_dropdown', '0', NULL),
(96, 8, 'array_filter', '', NULL),
(97, 8, 'array_filter_exclude', '', NULL),
(98, 8, 'assessment_value', '1', NULL),
(99, 8, 'display_columns', '1', NULL),
(100, 8, 'exclude_all_others', '', NULL),
(101, 8, 'exclude_all_others_auto', '0', NULL),
(102, 8, 'hidden', '0', NULL),
(103, 8, 'hide_tip', '0', NULL),
(104, 8, 'max_answers', '', NULL),
(105, 8, 'min_answers', '', NULL),
(106, 8, 'other_numbers_only', '0', NULL),
(107, 8, 'other_replace_text', '', NULL),
(108, 8, 'page_break', '0', NULL),
(109, 8, 'public_statistics', '0', NULL),
(110, 8, 'random_group', '', NULL),
(111, 8, 'random_order', '0', NULL),
(112, 8, 'scale_export', '0', NULL),
(113, 9, 'array_filter', '', NULL),
(114, 9, 'array_filter_exclude', '', NULL),
(115, 9, 'assessment_value', '1', NULL),
(116, 9, 'hidden', '0', NULL),
(117, 9, 'hide_tip', '0', NULL),
(118, 9, 'max_answers', '', NULL),
(119, 9, 'min_answers', '', NULL),
(120, 9, 'other_comment_mandatory', '0', NULL),
(121, 9, 'other_numbers_only', '0', NULL),
(122, 9, 'other_replace_text', '', NULL),
(123, 9, 'page_break', '0', NULL),
(124, 9, 'public_statistics', '0', NULL),
(125, 9, 'random_group', '', NULL),
(126, 9, 'random_order', '0', NULL),
(127, 9, 'scale_export', '0', NULL),
(128, 10, 'alphasort', '0', NULL),
(129, 10, 'hidden', '0', NULL),
(130, 10, 'hide_tip', '0', NULL),
(131, 10, 'page_break', '0', NULL),
(132, 10, 'public_statistics', '0', NULL),
(133, 10, 'random_group', '', NULL),
(134, 10, 'random_order', '0', NULL),
(135, 10, 'scale_export', '0', NULL),
(136, 11, 'equals_num_value', '', NULL),
(137, 11, 'hidden', '0', NULL),
(138, 11, 'hide_tip', '0', NULL),
(139, 11, 'max_num_value', '', NULL),
(140, 11, 'max_num_value_sgqa', '', NULL),
(141, 11, 'maximum_chars', '', NULL),
(142, 11, 'min_num_value', '', NULL),
(143, 11, 'min_num_value_sgqa', '', NULL),
(144, 11, 'num_value_equals_sgqa', '', NULL),
(145, 11, 'page_break', '0', NULL),
(146, 11, 'prefix', 'p', NULL),
(147, 11, 'public_statistics', '0', NULL),
(148, 11, 'random_group', '', NULL),
(149, 11, 'random_order', '0', NULL),
(150, 11, 'slider_accuracy', '', NULL),
(151, 11, 'slider_default', '', NULL),
(152, 11, 'slider_layout', '0', NULL),
(153, 11, 'slider_max', '', NULL),
(154, 11, 'slider_middlestart', '0', NULL),
(155, 11, 'slider_min', '', NULL),
(156, 11, 'slider_separator', '', NULL),
(157, 11, 'slider_showminmax', '0', NULL),
(158, 11, 'suffix', 's', NULL),
(159, 11, 'text_input_width', '', NULL),
(160, 12, 'hidden', '0', NULL),
(161, 12, 'hide_tip', '0', NULL),
(162, 12, 'max_answers', '', NULL),
(163, 12, 'min_answers', '', NULL),
(164, 12, 'page_break', '0', NULL),
(165, 12, 'public_statistics', '0', NULL),
(166, 12, 'random_group', '', NULL),
(167, 12, 'random_order', '0', NULL),
(168, 13, 'hidden', '0', NULL),
(169, 13, 'page_break', '0', NULL),
(170, 13, 'public_statistics', '0', NULL),
(171, 13, 'random_group', '', NULL),
(172, 13, 'scale_export', '0', NULL),
(173, 14, 'answer_width', '', NULL),
(174, 14, 'array_filter', '', NULL),
(175, 14, 'array_filter_exclude', '', NULL),
(176, 14, 'hidden', '0', NULL),
(177, 14, 'page_break', '0', NULL),
(178, 14, 'public_statistics', '0', NULL),
(179, 14, 'random_group', '', NULL),
(180, 14, 'random_order', '0', NULL),
(181, 14, 'scale_export', '0', NULL),
(182, 15, 'hidden', '0', NULL),
(183, 15, 'page_break', '0', NULL),
(184, 15, 'public_statistics', '0', NULL),
(185, 15, 'random_group', '', NULL),
(186, 15, 'random_order', '0', NULL),
(187, 15, 'scale_export', '0', NULL),
(188, 16, 'display_columns', '1', NULL),
(189, 16, 'hidden', '0', NULL),
(190, 16, 'page_break', '0', NULL),
(191, 16, 'public_statistics', '0', NULL),
(192, 16, 'random_group', '', NULL),
(193, 16, 'scale_export', '0', NULL),
(194, 17, 'alphasort', '0', NULL),
(195, 17, 'category_separator', '', NULL),
(196, 17, 'hidden', '0', NULL),
(197, 17, 'hide_tip', '0', NULL),
(198, 17, 'other_comment_mandatory', '0', NULL),
(199, 17, 'other_replace_text', '', NULL),
(200, 17, 'page_break', '0', NULL),
(201, 17, 'public_statistics', '0', NULL),
(202, 17, 'random_group', '', NULL),
(203, 17, 'random_order', '0', NULL),
(204, 17, 'scale_export', '0', NULL),
(205, 18, 'dropdown_dates', '0', NULL),
(206, 18, 'dropdown_dates_year_max', '', NULL),
(207, 18, 'dropdown_dates_year_min', '', NULL),
(208, 18, 'hidden', '0', NULL),
(209, 18, 'page_break', '0', NULL),
(210, 18, 'random_group', '', NULL),
(211, 18, 'reverse', '0', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `quota`
--

CREATE TABLE IF NOT EXISTS `quota` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sid` int(11) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `qlimit` int(8) DEFAULT NULL,
  `action` int(2) DEFAULT NULL,
  `active` int(1) NOT NULL DEFAULT '1',
  `autoload_url` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `quota_idx2` (`sid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `quota_languagesettings`
--

CREATE TABLE IF NOT EXISTS `quota_languagesettings` (
  `quotals_id` int(11) NOT NULL AUTO_INCREMENT,
  `quotals_quota_id` int(11) NOT NULL DEFAULT '0',
  `quotals_language` varchar(45) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `quotals_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `quotals_message` text COLLATE utf8_unicode_ci NOT NULL,
  `quotals_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `quotals_urldescrip` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`quotals_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `quota_members`
--

CREATE TABLE IF NOT EXISTS `quota_members` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sid` int(11) DEFAULT NULL,
  `qid` int(11) DEFAULT NULL,
  `quota_id` int(11) DEFAULT NULL,
  `code` varchar(11) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sid` (`sid`,`qid`,`quota_id`,`code`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `saved_control`
--

CREATE TABLE IF NOT EXISTS `saved_control` (
  `scid` int(11) NOT NULL AUTO_INCREMENT,
  `sid` int(11) NOT NULL DEFAULT '0',
  `srid` int(11) NOT NULL DEFAULT '0',
  `identifier` text COLLATE utf8_unicode_ci NOT NULL,
  `access_code` text COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(320) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ip` text COLLATE utf8_unicode_ci NOT NULL,
  `saved_thisstep` text COLLATE utf8_unicode_ci NOT NULL,
  `status` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `saved_date` datetime NOT NULL,
  `refurl` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`scid`),
  KEY `saved_control_idx2` (`sid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `sessions`
--

CREATE TABLE IF NOT EXISTS `sessions` (
  `sesskey` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `expiry` datetime NOT NULL,
  `expireref` varchar(250) COLLATE utf8_unicode_ci DEFAULT '',
  `created` datetime NOT NULL,
  `modified` datetime NOT NULL,
  `sessdata` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`sesskey`),
  KEY `sess2_expiry` (`expiry`),
  KEY `sess2_expireref` (`expireref`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `settings_global`
--

CREATE TABLE IF NOT EXISTS `settings_global` (
  `stg_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `stg_value` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`stg_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `settings_global`
--

INSERT INTO `settings_global` (`stg_name`, `stg_value`) VALUES
('DBVersion', '155'),
('SessionName', 'ls27979442643382196385'),
('force_ssl', ''),
('showxquestions', 'choose'),
('showgroupinfo', 'choose'),
('showqnumcode', 'choose'),
('updateavailable', '0'),
('updatelastcheck', '2012-03-20 16:02:40'),
('siteadminbounce', 'your-email@example.net');

-- --------------------------------------------------------

--
-- Table structure for table `surveys`
--

CREATE TABLE IF NOT EXISTS `surveys` (
  `sid` int(11) NOT NULL,
  `owner_id` int(11) NOT NULL,
  `admin` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `active` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'N',
  `expires` datetime DEFAULT NULL,
  `startdate` datetime DEFAULT NULL,
  `adminemail` varchar(320) COLLATE utf8_unicode_ci DEFAULT NULL,
  `anonymized` char(1) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'N',
  `faxto` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `format` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `savetimings` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `template` varchar(100) COLLATE utf8_unicode_ci DEFAULT 'default',
  `language` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `additional_languages` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `datestamp` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `usecookie` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `allowregister` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `allowsave` char(1) COLLATE utf8_unicode_ci DEFAULT 'Y',
  `autonumber_start` bigint(11) DEFAULT '0',
  `autoredirect` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `allowprev` char(1) COLLATE utf8_unicode_ci DEFAULT 'Y',
  `printanswers` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `ipaddr` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `refurl` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `datecreated` date DEFAULT NULL,
  `publicstatistics` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `publicgraphs` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `listpublic` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `htmlemail` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `tokenanswerspersistence` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `assessments` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `usecaptcha` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `usetokens` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `bounce_email` varchar(320) COLLATE utf8_unicode_ci DEFAULT NULL,
  `attributedescriptions` text COLLATE utf8_unicode_ci,
  `emailresponseto` text COLLATE utf8_unicode_ci,
  `emailnotificationto` text COLLATE utf8_unicode_ci,
  `tokenlength` tinyint(2) DEFAULT '15',
  `showxquestions` char(1) COLLATE utf8_unicode_ci DEFAULT 'Y',
  `showgroupinfo` char(1) COLLATE utf8_unicode_ci DEFAULT 'B',
  `shownoanswer` char(1) COLLATE utf8_unicode_ci DEFAULT 'Y',
  `showqnumcode` char(1) COLLATE utf8_unicode_ci DEFAULT 'X',
  `bouncetime` bigint(20) DEFAULT NULL,
  `bounceprocessing` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `bounceaccounttype` varchar(4) COLLATE utf8_unicode_ci DEFAULT NULL,
  `bounceaccounthost` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `bounceaccountpass` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `bounceaccountencryption` varchar(3) COLLATE utf8_unicode_ci DEFAULT NULL,
  `bounceaccountuser` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `showwelcome` char(1) COLLATE utf8_unicode_ci DEFAULT 'Y',
  `showprogress` char(1) COLLATE utf8_unicode_ci DEFAULT 'Y',
  `allowjumps` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `navigationdelay` tinyint(2) DEFAULT '0',
  `nokeyboard` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `alloweditaftercompletion` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `googleanalyticsstyle` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `googleanalyticsapikey` varchar(25) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`sid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `surveys`
--

INSERT INTO `surveys` (`sid`, `owner_id`, `admin`, `active`, `expires`, `startdate`, `adminemail`, `anonymized`, `faxto`, `format`, `savetimings`, `template`, `language`, `additional_languages`, `datestamp`, `usecookie`, `allowregister`, `allowsave`, `autonumber_start`, `autoredirect`, `allowprev`, `printanswers`, `ipaddr`, `refurl`, `datecreated`, `publicstatistics`, `publicgraphs`, `listpublic`, `htmlemail`, `tokenanswerspersistence`, `assessments`, `usecaptcha`, `usetokens`, `bounce_email`, `attributedescriptions`, `emailresponseto`, `emailnotificationto`, `tokenlength`, `showxquestions`, `showgroupinfo`, `shownoanswer`, `showqnumcode`, `bouncetime`, `bounceprocessing`, `bounceaccounttype`, `bounceaccounthost`, `bounceaccountpass`, `bounceaccountencryption`, `bounceaccountuser`, `showwelcome`, `showprogress`, `allowjumps`, `navigationdelay`, `nokeyboard`, `alloweditaftercompletion`, `googleanalyticsstyle`, `googleanalyticsapikey`) VALUES
(65284, 1, 'Your Name', 'Y', NULL, NULL, 'your-email@example.net', 'N', '', 'G', 'N', 'default', 'en', 'fr ', 'N', 'N', 'N', 'Y', 6, 'N', 'N', 'N', 'N', 'N', '2012-03-20', 'N', 'N', 'Y', 'Y', 'N', 'N', 'D', 'N', 'your-email@example.net', '', '', '', 15, 'Y', 'B', 'Y', 'X', 0, 'N', '', '', '', '', '', 'Y', 'Y', 'N', 0, 'N', 'N', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `surveys_languagesettings`
--

CREATE TABLE IF NOT EXISTS `surveys_languagesettings` (
  `surveyls_survey_id` int(10) unsigned NOT NULL DEFAULT '0',
  `surveyls_language` varchar(45) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `surveyls_title` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `surveyls_description` text COLLATE utf8_unicode_ci,
  `surveyls_welcometext` text COLLATE utf8_unicode_ci,
  `surveyls_endtext` text COLLATE utf8_unicode_ci,
  `surveyls_url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `surveyls_urldescription` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `surveyls_email_invite_subj` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `surveyls_email_invite` text COLLATE utf8_unicode_ci,
  `surveyls_email_remind_subj` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `surveyls_email_remind` text COLLATE utf8_unicode_ci,
  `surveyls_email_register_subj` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `surveyls_email_register` text COLLATE utf8_unicode_ci,
  `surveyls_email_confirm_subj` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `surveyls_email_confirm` text COLLATE utf8_unicode_ci,
  `surveyls_dateformat` int(10) unsigned NOT NULL DEFAULT '1',
  `email_admin_notification_subj` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email_admin_notification` text COLLATE utf8_unicode_ci,
  `email_admin_responses_subj` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email_admin_responses` text COLLATE utf8_unicode_ci,
  `surveyls_numberformat` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`surveyls_survey_id`,`surveyls_language`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `surveys_languagesettings`
--

INSERT INTO `surveys_languagesettings` (`surveyls_survey_id`, `surveyls_language`, `surveyls_title`, `surveyls_description`, `surveyls_welcometext`, `surveyls_endtext`, `surveyls_url`, `surveyls_urldescription`, `surveyls_email_invite_subj`, `surveyls_email_invite`, `surveyls_email_remind_subj`, `surveyls_email_remind`, `surveyls_email_register_subj`, `surveyls_email_register`, `surveyls_email_confirm_subj`, `surveyls_email_confirm`, `surveyls_dateformat`, `email_admin_notification_subj`, `email_admin_notification`, `email_admin_responses_subj`, `email_admin_responses`, `surveyls_numberformat`) VALUES
(65284, 'en', 'Questionnaire1', 'blabla', 'pouet', 'patate', '', '', 'Invitation to participate in a survey', 'Dear {FIRSTNAME},<br /><br />you have been invited to participate in a survey.<br /><br />The survey is titled:<br />"{SURVEYNAME}"<br /><br />"{SURVEYDESCRIPTION}"<br /><br />To participate, please click on the link below.<br /><br />Sincerely,<br /><br />{ADMINNAME} ({ADMINEMAIL})<br /><br />----------------------------------------------<br />Click here to do the survey:<br />{SURVEYURL}<br /><br />If you do not want to participate in this survey and don''t want to receive any more invitations please click the following link:<br />{OPTOUTURL}', 'Reminder to participate in a survey', 'Dear {FIRSTNAME},<br /><br />Recently we invited you to participate in a survey.<br /><br />We note that you have not yet completed the survey, and wish to remind you that the survey is still available should you wish to take part.<br /><br />The survey is titled:<br />"{SURVEYNAME}"<br /><br />"{SURVEYDESCRIPTION}"<br /><br />To participate, please click on the link below.<br /><br />Sincerely,<br /><br />{ADMINNAME} ({ADMINEMAIL})<br /><br />----------------------------------------------<br />Click here to do the survey:<br />{SURVEYURL}<br /><br />If you do not want to participate in this survey and don''t want to receive any more invitations please click the following link:<br />{OPTOUTURL}', 'Survey registration confirmation', 'Dear {FIRSTNAME},<br /><br />You, or someone using your email address, have registered to participate in an online survey titled {SURVEYNAME}.<br /><br />To complete this survey, click on the following URL:<br /><br />{SURVEYURL}<br /><br />If you have any questions about this survey, or if you did not register to participate and believe this email is in error, please contact {ADMINNAME} at {ADMINEMAIL}.', 'Confirmation of your participation in our survey', 'Dear {FIRSTNAME},<br /><br />this email is to confirm that you have completed the survey titled {SURVEYNAME} and your response has been saved. Thank you for participating.<br /><br />If you have any further questions about this email, please contact {ADMINNAME} on {ADMINEMAIL}.<br /><br />Sincerely,<br /><br />{ADMINNAME}', 1, 'Response submission for survey {SURVEYNAME}', 'Hello,<br /><br />A new response was submitted for your survey ''{SURVEYNAME}''.<br /><br />Click the following link to reload the survey:<br />{RELOADURL}<br /><br />Click the following link to see the individual response:<br />{VIEWRESPONSEURL}<br /><br />Click the following link to edit the individual response:<br />{EDITRESPONSEURL}<br /><br />View statistics by clicking here:<br />{STATISTICSURL}', 'Response submission for survey {SURVEYNAME} with results', '<style type="text/css">\n                                                .printouttable {\n                                                  margin:1em auto;\n                                                }\n                                                .printouttable th {\n                                                  text-align: center;\n                                                }\n                                                .printouttable td {\n                                                  border-color: #ddf #ddf #ddf #ddf;\n                                                  border-style: solid;\n                                                  border-width: 1px;\n                                                  padding:0.1em 1em 0.1em 0.5em;\n                                                }\n\n                                                .printouttable td:first-child {\n                                                  font-weight: 700;\n                                                  text-align: right;\n                                                  padding-right: 5px;\n                                                  padding-left: 5px;\n\n                                                }\n                                                .printouttable .printanswersquestion td{\n                                                  background-color:#F7F8FF;\n                                                }\n\n                                                .printouttable .printanswersquestionhead td{\n                                                  text-align: left;\n                                                  background-color:#ddf;\n                                                }\n\n                                                .printouttable .printanswersgroup td{\n                                                  text-align: center;        \n                                                  font-weight:bold;\n                                                  padding-top:1em;\n                                                }\n                                                </style>Hello,<br /><br />A new response was submitted for your survey ''{SURVEYNAME}''.<br /><br />Click the following link to reload the survey:<br />{RELOADURL}<br /><br />Click the following link to see the individual response:<br />{VIEWRESPONSEURL}<br /><br />Click the following link to edit the individual response:<br />{EDITRESPONSEURL}<br /><br />View statistics by clicking here:<br />{STATISTICSURL}<br /><br /><br />The following answers were given by the participant:<br />{ANSWERTABLE}', 0),
(65284, 'fr', 'Questionnaire numéro 1', 'dfdd', 'ddd', 'fff', '', '', 'Invitation à participer à un questionnaire', 'Cher(e) {FIRSTNAME},\n\nVous avez été invité à participer à un questionnaire.\n\nCelui-ci est intitulé :\n"{SURVEYNAME}"\n\n"{SURVEYDESCRIPTION}"\n\nPour participer, veuillez cliquer sur le lien ci-dessous.\n\nCordialement,\n\n{ADMINNAME} ({ADMINEMAIL})\n\n----------------------------------------------\nCliquez ici pour remplir ce questionnaire :\n{SURVEYURL}\n\nSi vous ne souhaitez pas participer à ce questionnaire et ne souhaitez plus recevoir aucune invitation, veuillez cliquer sur le lien suivant :\n{OPTOUTURL}', 'Rappel pour participer à un questionnaire', 'Cher(e) {FIRSTNAME},\n\nVous avez été invité à participer à un questionnaire récemment.\n\nNous avons pris en compte que vous n''avez pas encore complété le questionnaire, et nous vous rappelons que celui-ci est toujours disponible si vous souhaitez participer.\n\nLe questionnaire est intitulé :\n"{SURVEYNAME}"\n\n"{SURVEYDESCRIPTION}"\n\nPour participer, veuillez cliquer sur le lien ci-dessous.\n\nCordialement,\n\n{ADMINNAME} ({ADMINEMAIL})\n\n----------------------------------------------\nCliquez ici pour faire le questionnaire:\n{SURVEYURL}\n\nSi vous ne souhaitez pas participer à ce questionnaire et ne souhaitez plus recevoir aucune invitation, veuillez cliquer sur le lien suivant :\n{OPTOUTURL}', 'Confirmation d''enregistrement au questionnaire', 'Cher(e){FIRSTNAME},\n\nVous (ou quelqu''un utilisant votre adresse électronique) vous êtes enregistré pour participer à un questionnaire en ligne intitulé {SURVEYNAME}.\n\nPour compléter ce questionnaire, cliquez sur le lien suivant :\n\n{SURVEYURL}\n\nSi vous avez des questions à propos de ce questionnaire, ou si vous ne vous êtes pas enregistré pour participer à celui-ci et croyez que ce courriel est une erreur, veuillez contacter {ADMINNAME} sur {ADMINEMAIL}', 'Confirmation de votre participation à notre questionnaire', 'Cher(e) {FIRSTNAME},\n\nCe courriel vous confirme que vous avez complété le questionnaire intitulé {SURVEYNAME} et que votre réponse a été enregistrée. Merci pour votre participation.\n\nSi vous avez des questions à propos de ce courriel, veuillez contacter {ADMINNAME} sur {ADMINEMAIL}.\n\nCordialement,\n\n{ADMINNAME}', 2, 'Soumission de réponse pour le questionnaire {SURVEYNAME}', 'Bonjour,\n\nUne nouvelle réponse a été soumise pour votre questionnaire ''{SURVEYNAME}''.\n\nCliquer sur le lien suivant pour recharger votre questionnaire :\n{RELOADURL}\n\nCliquer sur le lien suivant pour voir la réponse :\n{VIEWRESPONSEURL}\n\nCliquer sur le lien suivant pour éditer la réponse :\n{EDITRESPONSEURL}\n\nVisualiser les statistiques en cliquant ici :\n{STATISTICSURL}\n\nles réponses suivantes ont été données par le participant :\n{ANSWERTABLE}', 'Soumission de réponse pour le questionnaire {SURVEYNAME} avec résultats', '<style type="text/css">\n                                                .printouttable {\n                                                  margin:1em auto;\n                                                }\n                                                .printouttable th {\n                                                  text-align: center;\n                                                }\n                                                .printouttable td {\n                                                  border-color: #ddf #ddf #ddf #ddf;\n                                                  border-style: solid;\n                                                  border-width: 1px;\n                                                  padding:0.1em 1em 0.1em 0.5em;\n                                                }\n\n                                                .printouttable td:first-child {\n                                                  font-weight: 700;\n                                                  text-align: right;\n                                                  padding-right: 5px;\n                                                  padding-left: 5px;\n\n                                                }\n                                                .printouttable .printanswersquestion td{\n                                                  background-color:#F7F8FF;\n                                                }\n\n                                                .printouttable .printanswersquestionhead td{\n                                                  text-align: left;\n                                                  background-color:#ddf;\n                                                }\n\n                                                .printouttable .printanswersgroup td{\n                                                  text-align: center;        \n                                                  font-weight:bold;\n                                                  padding-top:1em;\n                                                }\n                                                </style>Bonjour,\n\nUne nouvelle réponse a été soumise pour votre questionnaire ''{SURVEYNAME}''.\n\nCliquer sur le lien suivant pour recharger votre questionnaire :\n{RELOADURL}\n\nCliquer sur le lien suivant pour voir la réponse :\n{VIEWRESPONSEURL}\n\nCliquez sur le lien suivant pour éditer la réponse individuelle :\n{EDITRESPONSEURL}\n\nVisualiser les statistiques en cliquant ici :\n{STATISTICSURL}\n\n\nles réponses suivantes ont été données par le participant :\n{ANSWERTABLE}', 0);

-- --------------------------------------------------------

--
-- Table structure for table `survey_65284`
--

CREATE TABLE IF NOT EXISTS `survey_65284` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `submitdate` datetime DEFAULT NULL,
  `lastpage` int(11) DEFAULT NULL,
  `startlanguage` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `token` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X1` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X1other` text COLLATE utf8_unicode_ci,
  `65284X1X2` text COLLATE utf8_unicode_ci,
  `65284X1X8SQ001` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X8SQ002` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X8SQ003` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X9SQ001` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X9SQ001comment` text COLLATE utf8_unicode_ci,
  `65284X1X9SQ002` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X9SQ002comment` text COLLATE utf8_unicode_ci,
  `65284X1X10` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X10comment` text COLLATE utf8_unicode_ci,
  `65284X1X11SQ001` double DEFAULT NULL,
  `65284X1X11SQ002` double DEFAULT NULL,
  `65284X1X121` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X122` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X13` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X16` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X17` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X1X17other` text COLLATE utf8_unicode_ci,
  `65284X1X18` date DEFAULT NULL,
  `65284X2X3SQ001` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X3SQ002` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X4` text COLLATE utf8_unicode_ci,
  `65284X2X4_filecount` tinyint(4) DEFAULT NULL,
  `65284X2X5SQY1_SQX1` text COLLATE utf8_unicode_ci,
  `65284X2X5SQY1_SQX2` text COLLATE utf8_unicode_ci,
  `65284X2X5SQY2_SQX1` text COLLATE utf8_unicode_ci,
  `65284X2X5SQY2_SQX2` text COLLATE utf8_unicode_ci,
  `65284X2X6SQY1_SQX1` text COLLATE utf8_unicode_ci,
  `65284X2X6SQY1_SQX2` text COLLATE utf8_unicode_ci,
  `65284X2X6SQY2_SQX1` text COLLATE utf8_unicode_ci,
  `65284X2X6SQY2_SQX2` text COLLATE utf8_unicode_ci,
  `65284X2X7SQ001#0` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X7SQ001#1` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X7SQ002#0` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X7SQ002#1` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X14SQ001` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `65284X2X15SQ001` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=8 ;

--
-- Dumping data for table `survey_65284`
--

INSERT INTO `survey_65284` (`id`, `submitdate`, `lastpage`, `startlanguage`, `token`, `65284X1X1`, `65284X1X1other`, `65284X1X2`, `65284X1X8SQ001`, `65284X1X8SQ002`, `65284X1X8SQ003`, `65284X1X9SQ001`, `65284X1X9SQ001comment`, `65284X1X9SQ002`, `65284X1X9SQ002comment`, `65284X1X10`, `65284X1X10comment`, `65284X1X11SQ001`, `65284X1X11SQ002`, `65284X1X121`, `65284X1X122`, `65284X1X13`, `65284X1X16`, `65284X1X17`, `65284X1X17other`, `65284X1X18`, `65284X2X3SQ001`, `65284X2X3SQ002`, `65284X2X4`, `65284X2X4_filecount`, `65284X2X5SQY1_SQX1`, `65284X2X5SQY1_SQX2`, `65284X2X5SQY2_SQX1`, `65284X2X5SQY2_SQX2`, `65284X2X6SQY1_SQX1`, `65284X2X6SQY1_SQX2`, `65284X2X6SQY2_SQX1`, `65284X2X6SQY2_SQX2`, `65284X2X7SQ001#0`, `65284X2X7SQ001#1`, `65284X2X7SQ002#0`, `65284X2X7SQ002#1`, `65284X2X14SQ001`, `65284X2X15SQ001`) VALUES
(6, '2012-03-20 16:05:09', 2, 'en', '1234', 'A1', '', 'ddd', 'Y', 'Y', '', 'Y', 'fff', '', '', ' ', 'fdsfsd', 12, NULL, 'A2', '', 'N', 'M', 'A1', '', '2012-03-21', '1', '3', '', 0, 'fsdfsd', '', 'ee', '', '232', '242', '', '', 'A2', '', '', 'A1', 'I', ''),
(7, '2012-03-21 10:27:34', 2, 'en', '5678', 'A2', '', '', '', '', '', '', '', '', '', ' ', '', NULL, NULL, '', '', 'N', '', '', '', NULL, '2', '2', '[{ "title":"sql","comment":"comment file","size":"4.403","name":"survey_65284%20%282%29.png","filename":"fu_6rf2vrpjayuqauh","ext":"png"}]', 1, '', '', '', '', '', '', '', '', '', '', '', '', '', '');

-- --------------------------------------------------------

--
-- Table structure for table `survey_links`
--

CREATE TABLE IF NOT EXISTS `survey_links` (
  `participant_id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `token_id` int(11) NOT NULL,
  `survey_id` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  PRIMARY KEY (`participant_id`,`token_id`,`survey_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `survey_permissions`
--

CREATE TABLE IF NOT EXISTS `survey_permissions` (
  `sid` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  `permission` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `create_p` tinyint(1) NOT NULL DEFAULT '0',
  `read_p` tinyint(1) NOT NULL DEFAULT '0',
  `update_p` tinyint(1) NOT NULL DEFAULT '0',
  `delete_p` tinyint(1) NOT NULL DEFAULT '0',
  `import_p` tinyint(1) NOT NULL DEFAULT '0',
  `export_p` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`sid`,`uid`,`permission`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `survey_permissions`
--

INSERT INTO `survey_permissions` (`sid`, `uid`, `permission`, `create_p`, `read_p`, `update_p`, `delete_p`, `import_p`, `export_p`) VALUES
(65284, 1, 'assessments', 1, 1, 1, 1, 0, 0),
(65284, 1, 'translations', 0, 1, 1, 0, 0, 0),
(65284, 1, 'quotas', 1, 1, 1, 1, 0, 0),
(65284, 1, 'responses', 1, 1, 1, 1, 1, 1),
(65284, 1, 'statistics', 0, 1, 0, 0, 0, 0),
(65284, 1, 'surveyactivation', 0, 0, 1, 0, 0, 0),
(65284, 1, 'surveycontent', 1, 1, 1, 1, 1, 1),
(65284, 1, 'survey', 0, 1, 0, 1, 0, 0),
(65284, 1, 'surveylocale', 0, 1, 1, 0, 0, 0),
(65284, 1, 'surveysecurity', 1, 1, 1, 1, 0, 0),
(65284, 1, 'surveysettings', 0, 1, 1, 0, 0, 0),
(65284, 1, 'tokens', 1, 1, 1, 1, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `survey_url_parameters`
--

CREATE TABLE IF NOT EXISTS `survey_url_parameters` (
  `id` int(9) NOT NULL AUTO_INCREMENT,
  `sid` int(10) NOT NULL,
  `parameter` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `targetqid` int(10) DEFAULT NULL,
  `targetsqid` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `templates`
--

CREATE TABLE IF NOT EXISTS `templates` (
  `folder` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `creator` int(11) NOT NULL,
  PRIMARY KEY (`folder`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `templates_rights`
--

CREATE TABLE IF NOT EXISTS `templates_rights` (
  `uid` int(11) NOT NULL,
  `folder` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `use` int(1) NOT NULL,
  PRIMARY KEY (`uid`,`folder`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tokens_65284`
--

CREATE TABLE IF NOT EXISTS `tokens_65284` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastname` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` text COLLATE utf8_unicode_ci,
  `emailstatus` text COLLATE utf8_unicode_ci,
  `token` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `language` varchar(25) COLLATE utf8_unicode_ci DEFAULT NULL,
  `sent` varchar(17) COLLATE utf8_unicode_ci DEFAULT 'N',
  `remindersent` varchar(17) COLLATE utf8_unicode_ci DEFAULT 'N',
  `remindercount` int(11) DEFAULT '0',
  `completed` varchar(17) COLLATE utf8_unicode_ci DEFAULT 'N',
  `usesleft` int(11) DEFAULT '1',
  `validfrom` datetime DEFAULT NULL,
  `validuntil` datetime DEFAULT NULL,
  `mpid` int(11) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `tokens_65284_idx` (`token`),
  KEY `idx_tokens_65284_efl` (`email`(120),`firstname`,`lastname`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=3 ;

--
-- Dumping data for table `tokens_65284`
--

INSERT INTO `tokens_65284` (`tid`, `firstname`, `lastname`, `email`, `emailstatus`, `token`, `language`, `sent`, `remindersent`, `remindercount`, `completed`, `usesleft`, `validfrom`, `validuntil`, `mpid`) VALUES
(1, '', '', '', 'OK', '1234', 'en', 'N', 'N', 0, '2012-03-20 16:05', 10, NULL, NULL, NULL),
(2, '', '', '', 'OK', '5678', 'en', 'N', 'N', 0, '2012-03-21 10:27', 0, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `users_name` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `password` blob NOT NULL,
  `full_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `parent_id` int(10) unsigned NOT NULL,
  `lang` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(320) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_survey` tinyint(1) NOT NULL DEFAULT '0',
  `create_user` tinyint(1) NOT NULL DEFAULT '0',
  `delete_user` tinyint(1) NOT NULL DEFAULT '0',
  `superadmin` tinyint(1) NOT NULL DEFAULT '0',
  `configurator` tinyint(1) NOT NULL DEFAULT '0',
  `manage_template` tinyint(1) NOT NULL DEFAULT '0',
  `manage_label` tinyint(1) NOT NULL DEFAULT '0',
  `htmleditormode` varchar(7) COLLATE utf8_unicode_ci DEFAULT 'default',
  `templateeditormode` varchar(7) COLLATE utf8_unicode_ci DEFAULT 'default',
  `questionselectormode` varchar(7) COLLATE utf8_unicode_ci DEFAULT 'default',
  `one_time_pw` blob,
  `dateformat` int(10) unsigned NOT NULL DEFAULT '1',
  `participant_panel` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `users_name` (`users_name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=2 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`uid`, `users_name`, `password`, `full_name`, `parent_id`, `lang`, `email`, `create_survey`, `create_user`, `delete_user`, `superadmin`, `configurator`, `manage_template`, `manage_label`, `htmleditormode`, `templateeditormode`, `questionselectormode`, `one_time_pw`, `dateformat`, `participant_panel`) VALUES
(1, 'admin', 0x35653838343839386461323830343731353164306535366638646336323932373733363033643064366161626264643632613131656637323164313534326438, 'Your Name', 0, 'en', 'your-email@example.net', 1, 1, 1, 1, 1, 1, 1, 'default', 'default', 'default', NULL, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `user_groups`
--

CREATE TABLE IF NOT EXISTS `user_groups` (
  `ugid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `description` text COLLATE utf8_unicode_ci NOT NULL,
  `owner_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`ugid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `user_in_groups`
--

CREATE TABLE IF NOT EXISTS `user_in_groups` (
  `ugid` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  PRIMARY KEY (`ugid`,`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;