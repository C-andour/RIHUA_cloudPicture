package com.itrihua.caritas.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    /**
     * 根据分片键值(spaceId)动态生成分表名
     * @param availableTargetNames 当前可用目标表集合,即真实的表集合,如 picture_1,picture_2
     * @param preciseShardingValue 分片属性,在配置文件中所定义的sharding-column
     * @return 分表名(所需要查询的真实表名)
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue(); // 获取分片键值(spaceId)
        String logicTableName = preciseShardingValue.getLogicTableName(); // 获取逻辑表名 ,此处是picture
        // spaceId 为 null 表示查询所有图片
        if (spaceId == null) {
            return logicTableName;
        }
        // 根据 spaceId 动态生成分表名
        String realTableName = "picture_" + spaceId;
        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        } else {
            return logicTableName;
        }
    }


    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        // 不支持范围分表
        return new ArrayList<>();
    }

    @Override
    public Properties getProps() {
        // 不需要额外的配置
        return null;
    }

    @Override
    public void init(Properties properties) {
        //初始化,不需要改写
    }
}
