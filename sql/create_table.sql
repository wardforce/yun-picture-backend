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
create index phone_country_code_pk
    on user (phone_country_code);
-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                      null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;
