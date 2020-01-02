package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_spu")
public class Spu {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String title;// 标题
    private String subTitle;// 子标题

    @JsonIgnore
    private Boolean saleable;// 是否上架
    private Boolean valid;// 是否有效，逻辑删除用
    @JsonIgnore
    private Date createTime;// 创建时间

    @JsonIgnore
    private Date lastUpdateTime;// 最后修改时间

    @Transient
    private String cname;

    @Transient
    private String bname;

    @Transient
    private List<Sku> skus;// 名字可不能乱起，必须和页面传过来的名字吻合

    @Transient
    private SpuDetail spuDetail; // 名字可不能乱起，必须和页面传过来的名字吻合
}