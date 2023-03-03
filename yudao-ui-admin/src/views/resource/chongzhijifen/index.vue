<template>
  <div>
    <div class="con">
      <div class="con-1">支付宝充值(￥1=10积分)</div>
      <el-input v-model="zm" type="number" placeholder="请输入金额"></el-input>
      <img @click="pay(0)"
        src="https://img0.baidu.com/it/u=797819266,2810369824&fm=253&fmt=auto&app=138&f=PNG?w=596&h=331"
        mode="scaleToFill" />
    </div>
    <div class="con">
      <div class="con-1">微信充值(￥1=10积分)</div>
      <el-input v-model="wm" type="number" placeholder="请输入金额"></el-input>
      <img @click="pay(1)"
        src="https://img1.baidu.com/it/u=3210160154,2716386534&fm=253&fmt=auto&app=138&f=JPEG?w=660&h=421" alt="">
    </div>
  </div>
</template>

<script>
import { chongzhijifen } from '@/api/resource/chongzhijifen.js'

export default {
  data() {
    return {
      zm: 0,
      wm: 0
    }
  },

  methods: {
    pay(type) {
      let money;
      if (type == 0) {
        money = this.zm
      } else {
        money = this.wm
      }

      const params = {
        money,
        scores: 10 * parseInt(money)
      }

      console.log(money)

      if (money != 0) {
        chongzhijifen(params).then(res => {
          this.$modal.notifySuccess("创建订单成功");
        }).catch(err => {
          this.$modal.notifyError("创建订单失败")
        })
      } else {
        this.$modal.notifyError("请输入正确的金额")
      }

      // console.log(typeof money)
      // if (typeof money)


      // console.log(mtype)


    }
  }
}
</script>

<style lang="scss" scoped>
.con {
  background-color: #F8F9FE;
  width: 80%;
  height: 300px;
  // margin-left: 50px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);
  margin: 30px;
  padding: 30px;

  .con-1 {
    // margin-top: 30px;
    // margin-left: 30px;
    font-size: 25px;
    margin-bottom: 20px;
  }

  // input {
  //   border: none;
  //   background-color: #F5F5F5;
  //   border-radius: 2rem;
  //   text-indent: 20px;
  // }

  img {
    margin-top: 20px;
    width: 200px;
    display: block;
  }
}
</style>