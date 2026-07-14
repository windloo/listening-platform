package com.windloo.model.feign;
import com.windloo.model.dto.SubmitEncoderReq;
import org.springframework.stereotype.Component;

@Component
public class MediaEncoderFeignFallback implements MediaEncoderFeignClient {
    /**
     * 转码服务不可用时抛出异常，使 @GlobalTransactional 感知失败并回滚全局事务。
     * （不再静默吞掉错误，否则单集已写入但转码任务丢失，无法回滚。）
     */
    @Override
    public void submit(SubmitEncoderReq req) {
        throw new RuntimeException("media-encoder-service 不可用，全局事务将回滚");
    }
}