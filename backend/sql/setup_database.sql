-- create database
create database treasure_hunt_db;

-- use the database just created
use treasure_hunt_db;

-- create a user "treasure_hunt" with password "treasurehunt5901"
set global validate_password.policy = LOW;
set global validate_password.length = 6;
create user treasure_hunt@localhost identified by 'treasurehunt5901';
grant all on treasure_hunt_db.* to treasure_hunt@localhost;

-- create a table of users
create table users (
  user_id bigint unsigned not null auto_increment primary key,
  username varchar(20) not null,
  date_created datetime not null default current_timestamp,
  date_updated datetime not null default current_timestamp on update current_timestamp,
  date_last_login datetime,
  email varchar(255) not null,
  fname varchar(25) not null,
  is_verified enum ('true', 'false') not null default 'false',
  lname varchar(50) not null,
  password char(64) not null,
  points int unsigned not null default 0,
  picture_extension varchar(8),
  status enum ('active', 'suspended', 'deleted') not null default 'active'
) engine=innodb;

-- create unique index on email
create unique index uix_email on users(email);

-- create a table of groups
create table groups (
  group_id bigint unsigned not null auto_increment primary key,
  creator_id bigint unsigned not null,
  members json not null,
  status enum ('active', 'suspended', 'deleted') not null default 'active',
  foreign key (creator_id) references users(user_id)
) engine=innodb;

-- create a table of challenges
create table challenges (
  challenge_id bigint unsigned not null auto_increment primary key,
  is_active enum ('true', 'false') not null default 'false',
  creator_id bigint unsigned not null,
  group_id bigint unsigned,
  name varchar(25) not null,
  puzzle varchar(50) not null,
  status enum ('active', 'suspended', 'deleted') not null default 'active',
  foreign key (user_id) references users(user_id)
) engine=innodb;

-- create index on group_id and user_id
create index ix_creator_id on challenges(creator_id);
create index ix_group_id on challenges(group_id);
