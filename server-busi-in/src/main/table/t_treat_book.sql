ALTER TABLE t_treat_book ADD COLUMN visitTime VARCHAR(30) DEFAULT NULL COMMENT '实际上门时间';
ALTER TABLE t_treat_book ADD COLUMN recCustId BIGINT(20) DEFAULT NULL COMMENT '接待人';
