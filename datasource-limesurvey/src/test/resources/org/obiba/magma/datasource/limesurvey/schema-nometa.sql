DROP TABLE IF EXISTS VARIABLES;
DROP TABLE IF EXISTS VARIABLE_ATTRIBUTES;
DROP TABLE IF EXISTS CATEGORIES;
DROP TABLE IF EXISTS BONE_DENSITY;
DROP TABLE IF EXISTS MY_TABLE;

DROP TABLE IF EXISTS answers;
CREATE TABLE answers (
  qid INTEGER default 0 NOT NULL,
  code varchar(5) default '' NOT NULL,
  answer varchar(10000) NOT NULL,
  assessment_value INTEGER default 0 NOT NULL,
  sortorder INTEGER NOT NULL,
  language varchar(20) default 'en' NOT NULL,
  scale_id tinyint default 0 NOT NULL,
  PRIMARY KEY (qid,code,language,scale_id)
)

DROP TABLE IF EXISTS groups;
CREATE TABLE groups (
  gid INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  sid INTEGER default 0 NOT NULL,
  group_name varchar(100) default '' NOT NULL,
  group_order INTEGER default 0 NOT NULL,
  description varchar(10000),
  language varchar(20) default 'en' NOT NULL,
  randomization_group varchar(20) default '' NOT NULL,
  grelevance varchar(10000) default NULL,
  PRIMARY KEY (gid,language),  
)

DROP TABLE IF EXISTS questions;
CREATE TABLE questions (
  qid INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  parent_qid INTEGER default 0 NOT NULL,
  sid INTEGER default 0 NOT NULL,
  gid INTEGER default 0 NOT NULL,
  type char(1) default 'T' NOT NULL,
  title varchar(20) default '' NOT NULL,
  question varchar(10000) NOT NULL,
  preg varchar(10000),
  help varchar(10000),
  other char(1) default 'N' NOT NULL,
  mandatory char(1) default NULL,
  question_order INTEGER NOT NULL,
  language varchar(20) default 'en',
  scale_id tinyint default 0 NOT NULL,
  same_default tinyint default 0 NOT NULL,
  relevance varchar(10000),
  PRIMARY KEY (qid,language)
)

DROP TABLE IF EXISTS question_attributes;
CREATE TABLE question_attributes (
  qaid INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  qid INTEGER default 0 NOT NULL,
  attribute varchar(50) default NULL,
  value varchar(10000) default NULL,
  language varchar(20) default NULL,
  PRIMARY KEY (qaid)
)

DROP TABLE IF EXISTS surveys;
CREATE TABLE surveys (
  sid INTEGER NOT NULL,
  owner_id INTEGER NOT NULL,
  admin varchar(50) default NULL,
  active char(1) default 'N' NOT NULL,
  expires datetime default NULL,
  startdate datetime default NULL,
  adminemail varchar(320) default NULL,
  anonymized char(1) default 'N' NOT NULL ,
  faxto varchar(20) default NULL,
  format char(1) default NULL,
  savetimings char(1) default 'N',
  template varchar(100) default 'default',
  language varchar(50) default NULL,
  additional_languages varchar(255) default NULL,
  datestamp char(1) default 'N',
  usecookie char(1) default 'N',
  allowregister char(1) default 'N',
  allowsave char(1) default 'Y',
  autonumber_start INTEGER default '0',
  autoredirect char(1) default 'N',
  allowprev char(1) default 'Y',
  printanswers char(1) default 'N',
  ipaddr char(1) default 'N',
  refurl char(1) default 'N',
  datecreated date default NULL,
  publicstatistics char(1) default 'N',
  publicgraphs char(1) default 'N',
  listpublic char(1) default 'N',
  htmlemail char(1) default 'N',
  tokenanswerspersistence char(1) default 'N',
  assessments char(1) default 'N',
  usecaptcha char(1) default 'N',
  usetokens char(1) default 'N',
  bounce_email varchar(320) default NULL,
  attributedescriptions varchar(10000),
  emailresponseto varchar(10000) default NULL,
  emailnotificationto varchar(10000) default NULL,
  tokenlength tinyint default '15',
  showxquestions char(1) default 'Y',
  showgroupinfo char(1) default 'B',
  shownoanswer char(1) default 'Y',
  showqnumcode char(1) default 'X',
  bouncetime bigint,
  bounceprocessing varchar(1) default 'N',
  bounceaccounttype VARCHAR(4),
  bounceaccounthost VARCHAR(200),
  bounceaccountpass VARCHAR(100),
  bounceaccountencryption VARCHAR(3),
  bounceaccountuser VARCHAR(200),
  showwelcome char(1) default 'Y',
  showprogress char(1) default 'Y',
  allowjumps char(1) default 'N',
  navigationdelay tinyint default '0',
  nokeyboard char(1) default 'N',
  alloweditaftercompletion char(1) default 'N',
  googleanalyticsstyle char(1) DEFAULT NULL,
  googleanalyticsapikey VARCHAR(25) DEFAULT NULL,
   PRIMARY KEY(sid)
)

DROP TABLE IF EXISTS surveys_languagesettings;
CREATE TABLE surveys_languagesettings (
  surveyls_survey_id INT DEFAULT 0 NOT NULL,
  surveyls_language VARCHAR(45) DEFAULT 'en' NULL ,
  surveyls_title VARCHAR(200) NOT NULL,
  surveyls_description VARCHAR(10000) NULL,
  surveyls_welcometext VARCHAR(10000) NULL,
  surveyls_endtext VARCHAR(10000) NULL,
  surveyls_url VARCHAR(255) NULL,
  surveyls_urldescription VARCHAR(255) NULL,
  surveyls_email_invite_subj VARCHAR(255) NULL,
  surveyls_email_invite VARCHAR(10000) NULL,
  surveyls_email_remind_subj VARCHAR(255) NULL,
  surveyls_email_remind VARCHAR(10000) NULL,
  surveyls_email_register_subj VARCHAR(255) NULL,
  surveyls_email_register VARCHAR(10000) NULL,
  surveyls_email_confirm_subj VARCHAR(255) NULL,
  surveyls_email_confirm VARCHAR(10000) NULL,
  surveyls_dateformat INT DEFAULT 1 NOT NULL ,
  email_admin_notification_subj  VARCHAR(255) NULL,
  email_admin_notification VARCHAR(10000) NULL,
  email_admin_responses_subj VARCHAR(255) NULL,
  email_admin_responses VARCHAR(10000) NULL,
  surveyls_numberformat INT DEFAULT 0 NOT NULL,
  PRIMARY KEY (surveyls_survey_id, surveyls_language)
)

