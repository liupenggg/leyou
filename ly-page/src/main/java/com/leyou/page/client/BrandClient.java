package com.leyou.page.client;

import com.leyou.item.api.BrangApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface BrandClient extends BrangApi {
}
