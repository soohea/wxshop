package com.soohea.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.soohea.api.DataStatus;
import com.soohea.wxshop.WxshopApplication;
import com.soohea.wxshop.entity.Response;
import com.soohea.wxshop.generate.Goods;
import com.soohea.wxshop.generate.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URISyntaxException;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class GoodsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testCreateGoods() throws IOException, URISyntaxException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Shop shop = new Shop();
        shop.setName("我的微信店铺");
        shop.setDescription("我的小店开张啦");
        shop.setImgUrl("http://shopUrl");

        HttpResponse shopResponse = doHttpRequest("/api/v1/shop",
                "POST",
                shop,
                loginResponse.cookie);
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.body, new TypeReference<Response<Shop>>() {
        });

        assertEquals(SC_CREATED, shopResponse.code);
        assertEquals("我的微信店铺", shopInResponse.getData().getName());
        assertEquals("我的小店开张啦", shopInResponse.getData().getDescription());
        assertEquals("http://shopUrl", shopInResponse.getData().getImgUrl());
        assertEquals("ok", shopInResponse.getData().getStatus());
        assertEquals(shopInResponse.getData().getOwnerUserId(), loginResponse.user.getId());

        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");
        goods.setImgUrl("http://url");
        goods.setPrice(1000L);
        goods.setStock(10);
        goods.setShopId(shopInResponse.getData().getId());

        HttpResponse response = doHttpRequest("/api/v1/goods",
                "POST",
                goods,
                loginResponse.cookie);
        Response<Goods> goodsInResponse = objectMapper.readValue(response.body, new TypeReference<Response<Goods>>() {
        });
        assertEquals(SC_CREATED, response.code);
        assertEquals("肥皂", goodsInResponse.getData().getName());
        assertEquals(shopInResponse.getData().getId(), goodsInResponse.getData().getShopId());
        assertEquals("ok", goodsInResponse.getData().getStatus());


    }

    @Test
    public void return404IfGoodsToDeleteNotExist() throws IOException, URISyntaxException {
        String cookie = loginAndGetCookie().cookie;
        HttpResponse response = doHttpRequest(
                "/api/v1/goods/12345678", "DELETE", null, cookie);
        assertEquals(SC_NOT_FOUND, response.code);
    }

    @Test
    public void testUpdateGoods() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Goods goods = new Goods();
        goods.setId(2L);
        goods.setShopId(1L);
        goods.setName("NewName");
        goods.setDescription("NewDesc");
        goods.setDetails("NewDetails");
        goods.setImgUrl("NewUrl");
        goods.setPrice(12345L);
        goods.setStock(1111);
        goods.setStatus(DataStatus.DELETED.getName());

        Response<Goods> goodsResponse = doHttpRequest(
                "/api/v1/goods/2",
                "PATCH",
                goods,
                loginResponse.cookie)
                .asJsonObject(new TypeReference<Response<Goods>>() {
                });

        Assertions.assertEquals(2L, goodsResponse.getData().getId());
        Assertions.assertEquals(1L, goodsResponse.getData().getShopId());
        Assertions.assertEquals("NewName", goodsResponse.getData().getName());
        Assertions.assertEquals("NewDesc", goodsResponse.getData().getDescription());
        Assertions.assertEquals("NewDetails", goodsResponse.getData().getDetails());
        Assertions.assertEquals("NewUrl", goodsResponse.getData().getImgUrl());
        Assertions.assertEquals(12345L, goodsResponse.getData().getPrice());
        Assertions.assertEquals(1111, goodsResponse.getData().getStock());
    }

    @Test
    public void testGetGoodsById() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();
        Response<Goods> goodsResponse = doHttpRequest(
                "/api/v1/goods/2",
                "GET",
                null,
                loginResponse.cookie)
                .asJsonObject(new TypeReference<Response<Goods>>() {
                });

        assertEquals(2L, goodsResponse.getData().getId());
        assertEquals("goods2", goodsResponse.getData().getName());
        assertEquals("desc2", goodsResponse.getData().getDescription());
        assertEquals("details2", goodsResponse.getData().getDetails());
    }


}
