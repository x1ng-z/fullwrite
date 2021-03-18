create table opcserve
(
    serveid   int auto_increment
        primary key,
    servename varchar(200) not null,
    serveip   varchar(100) not null
);

create table point
(
    pointid    int auto_increment
        primary key,
    tag        varchar(300)  null comment 'opc点号名称
',
    notion     varchar(300)  null comment '点号中文名称
',
    type       varchar(100)  null comment 'float|bool',
    writeable  int default 0 null comment '1可写 0不可以写',
    opcserveid int           not null comment '那个opcserve的id
',
    resouce    varchar(300)  not null comment '数据来源opc/mes',
    standard   varchar(300)  null comment '标准位号(如MJDL)，磨机电流,或者根据编码规则进行添加点位'
);

