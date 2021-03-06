alter table xxjr_busi_in.t_work_list drop  column status;
alter table xxjr_busi_in.t_work_list drop  column updateTime;
alter table xxjr_busi_in.t_borrow_store_record change column `handleType` smallint(2) DEFAULT NULL COMMENT '0-接单 1-继续跟进 2-客户预约 3-签单 4-回款  5-不能做(退单)，失败 6-签单办理完成 7-门店退款成功 8-门店退款失败 9-已上门 10-预约未上门 11-签约失败 12-门店回款成功 13- 门店回款失败 14-不需要 15-设置专属单 16-取消专属单';
alter table xxjr_busi_in.t_work_list_fish change column createTime `allotTime` datetime DEFAULT NULL COMMENT '分配时间';
alter table xxjr_busi_in.t_work_list_fish add `custTel` varchar(11) DEFAULT NULL COMMENT '客户手机' AFTER `customerId`;
alter table xxjr_busi_in.t_work_list_fish add `custName` varchar(20) DEFAULT '0' COMMENT '客户姓名' AFTER `custTel`;   
alter table xxjr_busi_in.t_work_list_fish add `allotBy` varchar(11) DEFAULT NULL COMMENT '分配人' AFTER `allotTime`;
alter table xxjr_busi_in.t_work_list_fish add `allotDesc` varchar(100) DEFAULT NULL COMMENT '分配描述' AFTER `allotBy`;
alter table xxjr_busi_in.t_work_list_fish add `remark` varchar(50) DEFAULT NULL COMMENT '备用'AFTER `allotDesc`;
alter table xxjr_busi_in.t_work_list_fish add `finishTime` datetime DEFAULT NULL COMMENT '完成时间'AFTER `remark`;