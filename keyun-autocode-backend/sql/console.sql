-- 创建数据库并指定字符集和排序规则
CREATE DATABASE IF NOT EXISTS `keyun_autocode`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;


-- 创建用户表
CREATE TABLE `user`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
    `user_account`    varchar(50)  NOT NULL COMMENT '账号',
    `password`        varchar(100) NOT NULL COMMENT '密码',
    `username`        varchar(50)  NOT NULL DEFAULT '无名' COMMENT '用户昵称',
    `phone`           varchar(20)           DEFAULT NULL COMMENT '手机号',
    `email`           varchar(100)          DEFAULT NULL COMMENT '邮箱',
    `sex`             tinyint               DEFAULT 0 COMMENT '性别（0-未知，1-男，2-女）',
    `avatar`          varchar(255)          DEFAULT NULL COMMENT '头像URL',
    `status`          tinyint      NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-正常）',
    `role`            tinyint      NOT NULL DEFAULT 2 COMMENT '用户类型（0-超级管理员，1-管理员，2-普通用户）',
    `dept_id`         bigint                DEFAULT NULL COMMENT '所属部门ID',
    `last_login_time` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   varchar(50)           DEFAULT NULL COMMENT '最后登录IP',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       bigint                DEFAULT NULL COMMENT '创建人ID',
    `edit_by`         bigint                DEFAULT NULL COMMENT '更新人ID',
    `remark`          varchar(500)          DEFAULT NULL COMMENT '备注',
    `intro`           varchar(500)          DEFAULT '这个人很懒，什么都没留下~' COMMENT '用户简介',
    `is_deleted`      datetime              DEFAULT NULL COMMENT '逻辑删除标识（NULL-未删除，非NULL-删除时间）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`user_account`) COMMENT '用户名唯一索引（登录查询用）',
    KEY `idx_phone` (`phone`) COMMENT '手机号索引（登录/找回密码用）',
    KEY `idx_email` (`email`) COMMENT '邮箱索引（登录/找回密码用）',
    KEY `idx_status` (`status`) COMMENT '状态索引（用户状态筛选用）',
    KEY `idx_dept_id` (`dept_id`) COMMENT '部门ID索引（部门用户筛选用）',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引（按时间范围查询用）',
    KEY `idx_is_deleted` (`is_deleted`) COMMENT '逻辑删除索引（方便查询未删除或已删除用户）'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';


-- 应用表
create table app
(
    id            bigint auto_increment comment 'id' primary key,
    appName       varchar(256)                       null comment '应用名称',
    cover         varchar(512)                       null comment '应用封面',
    init_prompt   text                               null comment '应用初始化的 prompt',
    code_gen_type varchar(64)                        null comment '代码生成类型（枚举）',
    deploy_key    varchar(64)                        null comment '部署标识',
    deployed_time datetime                           null comment '部署时间',
    priority      int      default 0                 not null comment '优先级',
    user_id       bigint                             not null comment '创建用户id',
    edit_time     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     datetime DEFAULT NULL COMMENT '逻辑删除标识（NULL-未删除，非NULL-删除时间）',
    UNIQUE KEY uk_deploy_key (deploy_key), -- 确保部署标识唯一
    INDEX idx_appName (appName),           -- 提升基于应用名称的查询性能
    INDEX idx_user_id (user_id)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    message_type varchar(32)                        not null comment 'user/ai',
    app_id       bigint                             not null comment '应用id',
    user_id      bigint                             not null comment '创建用户id',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     datetime DEFAULT NULL COMMENT '逻辑删除标识（NULL-未删除，非NULL-删除时间）',
    INDEX idx_appId (app_id),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (create_time),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (app_id, create_time) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;


