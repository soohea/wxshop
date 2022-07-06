package com.soohea.wxshop.service;


import com.soohea.api.DataStatus;
import com.soohea.api.exceptions.HttpException;
import com.soohea.api.data.PageResponse;
import com.soohea.wxshop.generate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class GoodsService {
    private GoodsMapper goodsMapper;
    private ShopMapper shopMapper;

    public Map<Long, Goods> getIdToGoodsMap(List<Long> goodsId) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(goodsId);
        List<Goods> goods = goodsMapper.selectByExample(example);

        if (goods.stream().map(Goods::getShopId).collect(toSet()).size() != 1) {
            throw HttpException.badRequest("商品ID非法！");
        }
        return goods.stream().collect(toMap(Goods::getId, x -> x));

    }

    @Autowired
    public GoodsService(GoodsMapper goodsMapper, ShopMapper shopMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
    }

    public Goods createGoods(Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        System.out.println(UserContext.getCurrentUser().getId());
        System.out.println(shop.getOwnerUserId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setStatus(DataStatus.OK.getName());
            long id = goodsMapper.insert(goods);
            goods.setId(id);
            return goods;
        } else {
            throw HttpException.forbidden("无权访问！");
        }
    }

    public Goods updateGoods(long id, Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            Goods goodsInDb = goodsMapper.selectByPrimaryKey(id);
            if (goodsInDb == null) {
                throw HttpException.notFound("未找到");
            }
            goodsInDb.setName(goods.getName());
            goodsInDb.setDetails(goods.getDetails());
            goodsInDb.setDescription(goods.getDescription());
            goodsInDb.setImgUrl(goods.getImgUrl());
            goodsInDb.setPrice(goods.getPrice());
            goodsInDb.setStock(goods.getStock());
            goodsInDb.setUpdatedAt(new Date());

            goodsMapper.updateByPrimaryKey(goodsInDb);

            return goodsInDb;
        } else {
            throw HttpException.forbidden("无权访问！");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            throw HttpException.notFound("商品未找到！");
        }
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());

        if (shop != null && Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setStatus(DataStatus.DELETED.getName());
            goodsMapper.updateByPrimaryKey(goods);
            return goods;
        } else {
            throw HttpException.forbidden("无权访问！");
        }
    }


    public PageResponse<Goods> getGoods(Integer pageNum, Integer pageSize, Integer shopId) {

        int totalNumber = countGoods(shopId);
        int totalPage = (totalNumber + pageSize - 1) / pageSize;

        GoodsExample page = new GoodsExample();
        page.setLimit(pageSize);
        page.setOffset((pageNum - 1) * pageSize);
        List<Goods> pagedGoods = goodsMapper.selectByExample(page);
        return PageResponse.pagedData(pageNum, pageSize, totalPage, pagedGoods);
    }

    private int countGoods(Integer shopId) {
        GoodsExample goodsExample = new GoodsExample();
        if (shopId == null) {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
        } else {
            goodsExample.createCriteria()
                    .andStatusEqualTo(DataStatus.OK.getName())
                    .andShopIdEqualTo(shopId.longValue());
        }
        return (int) goodsMapper.countByExample(goodsExample);
    }

}
