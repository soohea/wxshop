package com.soohea.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import com.soohea.api.DataStatus;
import com.soohea.wxshop.WxshopApplication;
import com.soohea.wxshop.controller.ShoppingCartController;
import com.soohea.wxshop.entity.*;
import com.soohea.wxshop.generate.Goods;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class ShoppingCartIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void canQueryShoppingCartData() throws IOException, URISyntaxException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        PageResponse<ShoppingCartData> response = doHttpRequest("/api/v1/shoppingCart?pageNum=2&pageSize=1",
                "GET", null, loginResponse.cookie).asJsonObject(new TypeReference<PageResponse<ShoppingCartData>>() {
        });

        Assertions.assertEquals(2, response.getPageNum());
        Assertions.assertEquals(1, response.getPageSize());
        Assertions.assertEquals(2, response.getTotalPage());
        Assertions.assertEquals(1, response.getData().size());
        Assertions.assertEquals(2, response.getData().get(0).getShop().getId());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                response.getData().get(0).getGoods().stream()
                        .map(Goods::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(100L, 200L),
                response.getData().get(0).getGoods().stream()
                        .map(GoodsWithNumber::getPrice).collect(toList()));
        Assertions.assertEquals(Arrays.asList(200, 300),
                response.getData().get(0).getGoods().stream()
                        .map(GoodsWithNumber::getNumber).collect(toList()));
    }

    @Test
    public void canAddShoppingCartData() throws IOException, URISyntaxException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        ShoppingCartController.AddToShoppingCartRequest request = new ShoppingCartController.AddToShoppingCartRequest();
        ShoppingCartController.AddToShoppingCartItem item = new ShoppingCartController.AddToShoppingCartItem();
        item.setId(2L);
        item.setNumber(2);

        request.setGoods(Collections.singletonList(item));

        Response<ShoppingCartData> response = doHttpRequest("/api/v1/shoppingCart",
                "POST", request, loginResponse.cookie).asJsonObject(new TypeReference<Response<ShoppingCartData>>() {
        });

        Assertions.assertEquals(1L, response.getData().getShop().getId());
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                response.getData().getGoods().stream().map(Goods::getId).collect(toList()));
        Assertions.assertEquals(Sets.newHashSet(2, 100),
                response.getData().getGoods().stream().map(GoodsWithNumber::getNumber).collect(toSet()));
        Assertions.assertTrue(response.getData().getGoods().stream().allMatch(
                goods -> goods.getShopId() == 1L
        ));
    }

    @Test
    public void canDeleteShoppingCartData() throws IOException, URISyntaxException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        Response<ShoppingCartData> response = doHttpRequest("/api/v1/shoppingCart/5",
                "DELETE", null, loginResponse.cookie).asJsonObject(new TypeReference<Response<ShoppingCartData>>() {
        });

        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals(1, response.getData().getGoods().size());

        GoodsWithNumber goods = response.getData().getGoods().get(0);

        Assertions.assertEquals(4L, goods.getId());
        Assertions.assertEquals(200, goods.getNumber());
        Assertions.assertEquals(DataStatus.OK.toString().toLowerCase(), goods.getStatus());

    }


}
