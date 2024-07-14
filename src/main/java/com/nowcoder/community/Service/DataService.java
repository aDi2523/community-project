package com.nowcoder.community.Service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    //处理DAU和UV统计界面

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //将指定的IP记入UV
    public void recordUV(String Ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, Ip);
    }

    //统计指定日期范围内的UV
    public long calculateUV(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        List<String> UVkeyList = new ArrayList<>();
        while (!calendar.getTime().after(endDate)) {
            String redisKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            UVkeyList.add(redisKey);
            //每次循环结束增加1天，如果日期已经超过了结束日期则退出循环
            calendar.add(Calendar.DATE, 1);
        }

        String unionKey = RedisKeyUtil.getUVKey(df.format(startDate), df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(unionKey, UVkeyList.toArray());

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        return size;
    }


    //将指定用户记入DAU
    public void recordDAU(long userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    //统计指定日期范围内的DAU
    public long calculateDAU(Date startDate, Date endDate){
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        //整理该日期范围内的key
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        List<byte[]> DAUkeyList = new ArrayList<>();
        while (!calendar.getTime().after(endDate)) {
            String redisKey = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            DAUkeyList.add(redisKey.getBytes());
            //每次循环结束增加1天，如果日期已经超过了结束日期则退出循环
            calendar.add(Calendar.DATE, 1);
        }

        //进行or运算
        String redisKey = RedisKeyUtil.getDAUKey(df.format(startDate), df.format(endDate));
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(),
                        DAUkeyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }

}
