<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <!-- <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item> -->
      <!-- <el-form-item label="计数" prop="count">
        <el-input v-model="queryParams.count" placeholder="请输入计数" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item> -->
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="年" prop="year">
        <el-input v-model="queryParams.year" placeholder="请输入年" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="月" prop="month">
        <el-input v-model="queryParams.month" placeholder="请输入月" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="日" prop="day">
        <el-input v-model="queryParams.day" placeholder="请输入日" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['identity:keywords-tagscount:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          :loading="exportLoading" v-hasPermi="['identity:keywords-tagscount:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="关键词标识" align="center" prop="keywordsId" />
      <el-table-column label="关键词" align="center" prop="keywordsText" />
      <el-table-column label="标注" align="center" prop="tagsText" />
      <el-table-column label="创建时间" align="center" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime[0]) }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column label="计数" align="center" prop="count" /> -->
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="用户名称" align="center" prop="userName" />
      <!-- <el-table-column label="日" align="center" prop="day" />
      <el-table-column label="月" align="center" prop="month" />
      <el-table-column label="年" align="center" prop="year" /> -->
      <!-- <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['identity:keywords-tagscount:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['identity:keywords-tagscount:delete']">删除</el-button>
        </template>
      </el-table-column> -->
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="计数" prop="count">
          <el-input v-model="form.count" placeholder="请输入计数" />
        </el-form-item>
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="日" prop="day">
          <el-input v-model="form.day" placeholder="请输入日" />
        </el-form-item>
        <el-form-item label="月" prop="month">
          <el-input v-model="form.month" placeholder="请输入月" />
        </el-form-item>
        <el-form-item label="年" prop="year">
          <el-input v-model="form.year" placeholder="请输入年" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createKeywordsTagscount, updateKeywordsTagscount, deleteKeywordsTagscount, getKeywordsTagscount, getKeywordsTagscountPage, exportKeywordsTagscountExcel } from "@/api/identity/keywordsTagscount";

export default {
  name: "KeywordsTagscount",
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
      // 关键词标注统计;列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        createTime: [],
        count: null,
        userId: null,
        day: null,
        month: null,
        year: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getKeywordsTagscountPage(this.queryParams).then(response => {
        const year = this.queryParams.year
        const month = this.queryParams.month
        const day = this.queryParams.day
        const resList = response.data.list
        resList.forEach(item => {
          const time = this.parseTime(item.createTime[0])
          const leftTime = time.split(' ')[0]
          const [year, month, day] = leftTime.split('-')
          item.year = year
          item.month = month
          item.day = day
        })
        let filterArray = resList
        if (year) {
          // console.log()
          filterArray = filterArray.filter(item => item.year == this.queryParams.year)
        }
        if (month) {
          filterArray = filterArray.filter(item => item.month == (this.queryParams.month.length == 1 ? '0' + this.queryParams.month : this.queryParams.month))
        }
        if (day) {
          filterArray = filterArray.filter(item => item.day == (this.queryParams.day.length == 1 ? '0' + this.queryParams.day : this.queryParams.day))
        }
        console.log(filterArray)
        this.list = filterArray;
        this.total = filterArray.length;
        this.loading = false;
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
        count: undefined,
        userId: undefined,
        day: undefined,
        month: undefined,
        year: undefined,
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
      this.title = "添加关键词标注统计;";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getKeywordsTagscount(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改关键词标注统计;";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateKeywordsTagscount(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createKeywordsTagscount(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除关键词标注统计;编号为"' + id + '"的数据项?').then(function () {
        return deleteKeywordsTagscount(id);
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
      this.$modal.confirm('是否确认导出所有关键词标注统计;数据项?').then(() => {
        this.exportLoading = true;
        return exportKeywordsTagscountExcel(params);
      }).then(response => {
        this.$download.excel(response, '关键词标注统计;.xls');
        this.exportLoading = false;
      }).catch(() => { });
    }
  }
};
</script>
