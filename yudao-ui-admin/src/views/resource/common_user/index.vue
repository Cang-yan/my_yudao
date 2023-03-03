<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <div class="jifen">
      <span class="jifen-1">您的积分余额：</span>
      <span class="jifen-2">{{ scoreAll }}</span>
    </div>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          :loading="exportLoading">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <!-- <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="id" prop="apiResourcesId">
        <el-input v-model="queryParams.apiResourcesId" placeholder="请输入api资源id" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="剩余次数" prop="count">
        <el-input v-model="queryParams.count" placeholder="请输入剩余调用次数" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form> -->

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="应用编号" align="center" prop="appId" />
      <el-table-column label="应用名称" align="center" prop="applycationName" />
      <el-table-column label="剩余调用次数" align="center" prop="remainApiCount" />
      <el-table-column label="创建时间" align="center" prop="createTime" />
      <el-table-column label="API Key" align="center" prop="apicallDOId" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <!-- <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['resource:apicount:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['resource:apicount:delete']">删除</el-button> -->
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleSecretKey(scope.row)"
            v-hasPermi="['resource:apicount:delete']" v-show="!scope.row.secret">查看密钥</el-button>
          <span v-show="scope.row.secret">{{ scope.row.secret }}</span>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total ? total : 0" :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="应用名称" prop="applicationName">
          <el-input v-model="form.applicationName" placeholder="请输入应用名称" />
        </el-form-item>
        <!-- <el-form-item label="剩余调用次数" prop="count">
          <el-input v-model="form.count" placeholder="请输入剩余调用次数" />
        </el-form-item> -->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="'请选择要为哪个应用充值'" :visible.sync="scoreOpen" width="500px" v-dialogDrag append-to-body>
      <el-select v-model="selectApp" placeholder="请选择">
        <el-option v-for="item in list" :key="item.appId" :label="item.applycationName" :value="item.apicallDOId">
        </el-option>
      </el-select>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmBuy">购买</el-button>
        <el-button @click="cancelBye">取 消</el-button>
      </div>
    </el-dialog>

    <div class="card-container">
      <div v-for="(item, index) in goodList" :key="index" style="cursor: pointer">
        <div class="card">
          <div class="title">超值套餐</div>
          <div class="price">{{ item.money }}积分</div>
          <!-- <div class="info">{{item.apiResourceCount}}</div> -->
          <div class="day">
            <span class="day-1">接口调用次数</span>
            <span class="day-2">{{ item.apiResourceCount }}</span>
          </div>
          <div class="day">
            <span class="day-1">普通节点流量</span>
            <span class="day-2">无限</span>
          </div>
          <div class="day">
            <span class="day-1">高速节点流量</span>
            <span class="day-2">5G</span>
          </div>
          <el-button type="primary" @click="handleBuy(item)">购买</el-button>
        </div>
      </div>
    </div>
    <pagination v-show="goodTotal > 0" :total="goodTotal" :page.sync="goodsQueryParams.pageNo"
      :limit.sync="goodsQueryParams.pageSize" @pagination="getGoodsList" />
  </div>
</template>

<script>
import { bugScoreAPI, secretkey, createApicount, updateApicount, deleteApicount, getApicount, getApicountPage, exportApicountExcel } from "@/api/resource/common_user.js";
import { createProduct, updateProduct, deleteProduct, getProduct, getProductPage, exportProductExcel } from "@/api/mall/product";
export default {
  name: "Apicount",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 调用次数;列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        apiResourcesId: null,
        count: null,
        createTime: [],
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      goodsQueryParams: {
        pageNo: 1,
        pageSize: 4,
        createTime: [],
        apiResourceCount: null,
        money: null,
        type: null,
      },
      goodList: [],
      goodTotal: 0,
      scoreOpen: false,
      selectApp: '',
      openProductId: '',
      scoreAll: 0
    };
  },
  created() {
    this.getList();
    this.getGoodsList()
  },
  methods: {
    cancelBye() {
      this.scoreOpen = false
    },
    confirmBuy() {
      const data = {
        apicallId: this.selectApp,
        mallProductId: this.openProductId
      }
      bugScoreAPI(data).then(res => {
        if (res.code === 0 && res.data === true) {
          this.$message.success('充值成功！')
          this.scoreOpen = false
        } else {
          this.$message.error('充值失败')
        }
      }).catch(err => {
        this.$message.error(err)
      })
    },
    handleBuy(item) {
      this.scoreOpen = true
      this.openProductId = item.id
    },
    getGoodsList() {
      // 执行查询
      getProductPage(this.goodsQueryParams).then(response => {
        this.goodList = response.data.list;
        this.goodTotal = response.data.total;
        // console.log(response)
      });
    },
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getApicountPage(this.queryParams).then(response => {
        this.list = response.data.apicallAppRespVOList;
        this.total = response.data.apicallAppRespVOList.length;
        this.loading = false;
        this.scoreAll = response.data.scores
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        apiResourcesId: undefined,
        count: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新建应用";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getApicount(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改调用次数;";
      });
    },
    handleSecretKey(row) {
      console.log(row)
      const id = row.appId
      secretkey(id).then(res => {
        // console.log(res)
        // row.secret = res.data
        // 这里是一个权宜之计，因为实在是新添加对象它无法做到响应式，只有在第一次赋值后的才响应
        // row.pageNo = res.data
        // this.list.push(row)
        // this.list = [...this.list]
        // 直接给对象中添加属性的话，视图不会更新，需要用$set这种vue的方式来给数组赋值
        this.$set(row, 'secret', res.data)
      })

    },
    /** 提交按钮 */
    submitForm() {
      const that = this
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.applicationName != null) {
          updateApicount(this.form.applicationName).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createApicount(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除调用次数;编号为"' + id + '"的数据项?').then(function () {
        return deleteApicount(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = { ...this.queryParams };
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.$modal.confirm('是否确认导出所有调用次数;数据项?').then(() => {
        this.exportLoading = true;
        return exportApicountExcel(params);
      }).then(response => {
        this.$download.excel(response, '调用次数;.xls');
        this.exportLoading = false;
      }).catch(() => { });
    }
  }
};
</script>

<style lang="scss" scoped>
.card-container {
  margin-top: 40px;
  display: flex;
  justify-content: space-around;
}

.card {
  background-color: #F8F9FE;
  padding: 20px;
  width: 300px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12),
    0 0 6px rgba(0, 0, 0, .04);
  display: flex;
  flex-direction: column;
  align-items: center;

  .title {
    font-size: 24px;
    color: #9BAECB;
  }

  .price {
    color: #526783;
    font-size: 26px;
  }

  .info {
    color: #526783;
  }

  .day {
    display: flex;
    justify-content: space-between;
    width: 200px;
    margin-bottom: 10px;
    margin-top: 10px;

    .day-1 {
      color: #526783;
      font-size: 18px;
    }

    .day-2 {
      color: #9BAECB;
      font-size: 20px;
    }
  }


}

.jifen {
  margin-bottom: 20px;

  .jifen-1 {
    font-size: 28px;
    font-weight: bold;
  }

  .jifen-2 {
    font-size: 32px;
    font-weight: bold;
    color: red;
  }
}
</style>
