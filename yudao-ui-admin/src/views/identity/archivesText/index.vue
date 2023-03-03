<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <!-- <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="电子版正文_如果有" prop="text">
        <el-input v-model="queryParams.text" placeholder="请输入电子版正文_如果有" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="ocr正文，如果有" prop="ocrText">
        <el-input v-model="queryParams.ocrText" placeholder="请输入ocr正文，如果有" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="档案id" prop="archivesId">
        <el-input v-model="queryParams.archivesId" placeholder="请输入档案id" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form> -->

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['identity:archives-text:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          :loading="exportLoading" v-hasPermi="['identity:archives-text:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="电子版正文" align="center" prop="text" />
      <el-table-column label="ocr正文" align="center" prop="ocrText" />
      <el-table-column label="档案ID" align="center" prop="archivesId" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['identity:archives-text:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['identity:archives-text:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="电子版正文" prop="text">
          <el-input v-model="form.text" placeholder="请输入电子版正文_如果有" />
        </el-form-item>
        <el-form-item label="ocr正文" prop="ocrText">
          <el-input v-model="form.ocrText" placeholder="请输入ocr正文，如果有" />
        </el-form-item>
        <!-- <el-form-item label="档案id" prop="archivesId">
          <el-input v-model="form.archivesId" placeholder="请输入档案id" />
        </el-form-item> -->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createArchivesText, updateArchivesText, deleteArchivesText, getArchivesText, getArchivesTextPage, exportArchivesTextExcel } from "@/api/identity/archivesText";

export default {
  name: "ArchivesText",
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
      // 档案正文;列表
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
        text: null,
        ocrText: null,
        archivesId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      userName: '',
      userId: '',
      role: '',
      RuserId: ''
    };
  },
  created() {
    this.userName = this.$store.state.user.nickname
    this.userId = this.$auth.hasRole("marker_custom") ? undefined : this.$store.state.user.id
    console.log(this.userName, this.userId)
    this.role = this.$auth.hasRole("marker_custom") ? 1 : 0
    this.RuserId = this.$store.state.user.id
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getArchivesTextPage({ ...this.queryParams, userId: this.userId }).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
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
        text: undefined,
        ocrText: undefined,
        archivesId: undefined,
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
      this.title = "添加档案正文";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getArchivesText(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改档案正文;";
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
          updateArchivesText(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createArchivesText(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除档案正文;编号为"' + id + '"的数据项?').then(function () {
        return deleteArchivesText(id);
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
      this.$modal.confirm('是否确认导出所有档案正文;数据项?').then(() => {
        this.exportLoading = true;
        return exportArchivesTextExcel(params);
      }).then(response => {
        this.$download.excel(response, '档案正文;.xls');
        this.exportLoading = false;
      }).catch(() => { });
    }
  }
};
</script>
