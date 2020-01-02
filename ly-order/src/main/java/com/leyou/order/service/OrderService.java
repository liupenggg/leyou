package com.leyou.order.service;

import com.github.pagehelper.PageHelper;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayStateEnum;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;


    public Long createOrder(OrderDTO orderDTO) {
        // 1.新增订单
        Order order = new Order();
        // 1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);// id
        order.setCreateTime(new Date());// 创建时间
        order.setPaymentType(orderDTO.getPaymentType());// 支付类型，1、在线支付，2、货到付款
        // 1.2 用户信息
        UserInfo user = UserInterceptor.getLoginUser();
        order.setUserId(user.getId());// 用户id
        order.setBuyerNick(user.getUserName());// 买家昵称
        order.setBuyerRate(false);// 买家是否已经评价
        // 1.3 收货人地址
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());// 收货人全名
        order.setReceiverAddress(addr.getAddress());// 收货地址，如：xx路xx号
        order.setReceiverCity(addr.getCity());// 城市
        order.setReceiverDistrict(addr.getDistrict());// 区/县
        order.setReceiverMobile(addr.getPhone());// 移动电话
        order.setReceiverState(addr.getState());// 省份
        order.setReceiverZip(addr.getZipCode());// 邮政编码,如：310001
        // 1.4 金额
        Map<Long, Integer> numMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        // 获取所有sku的id
        Set<Long> ids = numMap.keySet();
        // 根据id查询skus
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));
        // 准备orderDetaj集合
        List<OrderDetail> details = new ArrayList<>();
        long totalPay = 0L;
        for (Sku sku : skus) {
            // 删除购物车
//            cartClient.deleteCartById(String.valueOf(sku.getId()));   //TODO 401异常
//            try {
//            cartClient.deleteCartById(sku.getId().toString());
//            }catch (Exception e){
//                log.error("[创建订单服务] 删除购物车商品异常，{}",e);
//            }
            // 计算商品总价
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            // 封装订单详情orderDetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));// 图片
            orderDetail.setNum(numMap.get(sku.getId()));// 商品购买数量
            orderDetail.setOrderId(orderId);// 订单id
            orderDetail.setOwnSpec(sku.getOwnSpec());// 商品规格数据
            orderDetail.setPrice(sku.getPrice());// 商品单价
            orderDetail.setSkuId(sku.getId());// 商品id
            orderDetail.setTitle(sku.getTitle());// 商品标题

            details.add(orderDetail);
        }
        order.setTotalPay(totalPay);// 总金额
        // 实付金额 = 总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0);// 实付金额
        // 1.5 把order写入数据库
        int count = orderMapper.insertSelective(order);// 有选择性的去新增
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // 2 新增订单详情
        count = detailMapper.insertList(details);
        if (count != details.size()) {
            log.error("[创建订单] 创建订单详情失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_DETAIL_ERROR);
        }
        // 3 新增订单状态
        OrderStatus status = new OrderStatus();
        status.setCreateTime(order.getCreateTime());// 创建时间
        status.setOrderId(orderId);//
        status.setStatus(OrderStatusEnum.UN_PAY.getCode());//
        count = statusMapper.insertSelective(status);//
        if (count != 1) {
            log.error("[创建订单] 创建订单状态失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_STATUS_ERROR);
        }
        // 4 库存减少，采用乐观锁，解决高并发
        List<CartDTO> cartDTOS =orderDTO.getCarts();
        goodsClient.decreaseStock(cartDTOS);

        return orderId;
    }

    // ***********************************************************************************
    public Order queryOrderById(Long id) {
        // 查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);//
        List<OrderDetail> orderDetails = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(orderDetails)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(orderDetails);
        // 查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        // 更新订单状态
        order.setOrderStatus(orderStatus);

        return order;
    }

    public String creatPayUrl(Long orderId) {
        // 查询订单
        Order order = queryOrderById(orderId);
        // 判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.getCode()) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        // 实际金额
        // Long actualPay = order.getActualPay();
        Long actualPay = 1L;        //TODO  这里设置为1分钱，仅为测试使用
        // 商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String title = detail.getTitle();
        return payHelper.createPayUrl(orderId,actualPay,title);
    }

    // 处理回调
    public void handleNotify(Map<String, String> result) {
        // 数据校验
        payHelper.isSuccess(result);
        // 校验签名
        payHelper.isValidSign(result);
        // 检验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // 获取微信支付结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);

        // 获取订单金额
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        // TODO 这里应该是判断是否等于 order.getActualPay()才对，因为前面支付测试是1分钱，所以这里也是一分钱
        if (totalFee != /*order.getActualPay()*/ 1L) {
            // 微信支付金额与订单金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        // 修改订单状态
        payHelper.queryOrderState(orderId);
        log.info("[订单回调] 订单支付成功，订单编号:{}",orderId);
    }

    public PayStateEnum queryOrderState(Long orderId) {
        // 查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if (status != OrderStatusEnum.UN_PAY.getCode()) {
            // 如果订单表里是已支付状态，就一定是已支付
            return PayStateEnum.SUCCESS;
        }
        // 如果订单表里是未支付，不一定是未支付，必须去微信
        return payHelper.queryOrderState(orderId);
    }

    public List<Order> queryOrdersByUserId(Integer page, Integer rows) {
        // 分页
        PageHelper.startPage(page, rows);
        // TODO 未完成
        return null;
    }
}