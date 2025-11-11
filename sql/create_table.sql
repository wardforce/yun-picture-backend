-- 创建库
create database if not exists yun_picture;
use yun_picture;
-- 用户表
create table if not exists user
(
    id                 bigint auto_increment comment 'id'
        primary key,
    user_account       varchar(256)                           not null comment '账号',
    user_password      varchar(512)                           not null comment '密码',
    user_name          varchar(256)                           null comment '用户昵称',
    user_avatar        varchar(1024)                          null comment '用户头像',
    user_profile       varchar(512)                           null comment '用户简介',
    user_role          varchar(256) default 'user'            not null comment '用户角色：user/admin',
    edit_time          datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time        datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete          tinyint      default 0                 not null comment '是否删除',
    vip_expire_time    datetime                               null comment '会员过期时间',
    vip_code           varchar(128)                           null comment '会员兑换码',
    vip_number         bigint                                 null comment '会员编号',
    share_code         varchar(20)                            null comment '分享码',
    invite_user        bigint                                 null comment '邀请用户 id',
    phone_number       int                                    null,
    email              varchar(255)                           null,
    phone_country_code varchar(5)                             null,
    vip_level          varchar(256) default 'standard'        null comment '会员等级：standard,pro,max',
    constraint phone_country_code_pk
        unique (phone_country_code),
    constraint uk_userAccount
        unique (user_account),
    constraint user_phone_number_uindex
        unique (phone_number),
    constraint user_pk_2
        unique (email),
    constraint user_share_code_uindex
        unique (share_code),
    constraint vipCode_pk
        unique (vip_code)
)
    comment '用户' collate = utf8mb4_unicode_ci;
create index idx_userName
    on user (user_name);
