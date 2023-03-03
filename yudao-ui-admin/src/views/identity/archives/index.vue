<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <!-- <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="年代，按照年月日划分吧" prop="date">
        <el-date-picker v-model="queryParams.date" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="档案号" prop="refNum">
        <el-input v-model="queryParams.refNum" placeholder="请输入档案号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="0未标注 1有租户标注 2有标注员 3两个都有" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择0未标注 1有租户标注 2有标注员 3两个都有" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户id" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户id" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form> -->

    <!-- 文件夹的位置 -->
    <div class="folder-wraper" v-if="isAdmin">
      <el-button @click="checkFolder(0)">全部
      </el-button>
      <el-button @click="checkFolder(1)">未分类
      </el-button>
      <el-button v-for="(item, index) in folderList.archivesExtendsRespFolderVOS"
        @click="checkFolder(item.archivesExtendsRespViewVOS, item.folderId)" :key="index">{{ item.title }}
      </el-button>
    </div>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8" v-if="isAdmin">
      <el-col :span="1.5">
        <!-- <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['identity:archives:create']">新增</el-button> -->
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <!-- <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          :loading="exportLoading" v-hasPermi="['identity:archives:export']">导出</el-button> -->
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          :loading="exportLoading">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="档案号" align="center" prop="refNum" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" />
      <el-table-column label="年代" align="center" prop="date" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.date) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="userName" />
      <el-table-column :label="'标注情况'" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ labelFormat(scope.row.status) }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column label="用户id" align="center" prop="userId" /> -->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" v-if="isAdmin">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          <!-- <el-button size="mini" type="text" @click="handleBiaozhu(scope.row)"
            v-hasPermi="['identity:archives:delete']">标注</el-button> -->
          <el-button size="mini" type="text" @click="handleBiaozhu(scope.row)">标注</el-button>
          <el-button size="mini" type="text" @click="handleAddFolder(scope.row)">添加到文件夹</el-button>
          <!-- <el-button size="mini" type="text" @click="handleBiaozhu(scope.row)"
            v-hasPermi="['identity:archives:delete']">查看详情</el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0 && isAll" :total="total" :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="年代" prop="date">
          <el-date-picker clearable v-model="form.date" type="date" value-format="timestamp" placeholder="选择年代" />
        </el-form-item>
        <el-form-item label="档案号" prop="refNum">
          <el-input v-model="form.refNum" placeholder="请输入档案号" />
        </el-form-item>
        <!-- <el-form-item label="标注情况" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item> -->
        <!-- <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item> -->
        <el-form-item label="电子正文" prop="ocrText">
          <el-input v-model="form.ocrText" placeholder="请输入电子正文" />
        </el-form-item>
        <el-form-item label="正文" prop="text">
          <el-input v-model="form.text" placeholder="请输入正文" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="'查看详情'" :visible.sync="detailOpen" width="1000px" v-dialogDrag append-to-body>
      <div class="tag-cotnainer">
        <div class="left">
          {{ ocrText }}
          <el-table stripe :data="tableData" style="margin-top: 20px">
            <el-table-column label="标注人" prop="name"></el-table-column>
            <el-table-column label="标签" prop="tag"></el-table-column>
            <el-table-column label="身份" prop="identity"></el-table-column>
          </el-table>
        </div>
        <div class="right">
          <div>
            请标注
          </div>
          <div class="sql-container">
            <div v-for="(item, index) in tagList" :key="index">
              <div class="sql-title">{{ item.storeName }}</div>
              <div class="radio-container">
                <!-- <el-checkbox label="备选项1" border v-for="(item1, index1) in item.tagsMetaExtendsRespVOList" :key="index1"
                  :label="item1.tagsText" v-model="item1.checked" @change="handleItemChange(item1)"></el-checkbox> -->
                <el-radio v-model="tagChecked" :label="item1.tagsText" border
                  v-for="(item1, index1) in item.tagsMetaExtendsRespVOList" @change="handleItemChange(item1)"
                  :key="index1">{{ item1.tagsText }}</el-radio>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitTag">确 定</el-button>
        <el-button @click="cancelTag">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="'添加到文件夹'" :visible.sync="folderOpen" width="500px" v-dialogDrag append-to-body>
      <el-radio-group v-model="folderIndex">
        <el-radio :label="item.folderId" :key="index" style="margin-right: 10px; margin-bottom: 10px"
          v-for="(item, index) in folderList.archivesExtendsRespFolderVOS">
          {{
              item.title
          }}
        </el-radio>
      </el-radio-group>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="addNewFolder">新增文件夹</el-button>
        <el-button type="primary" @click="addTofolder">确 定</el-button>
        <el-button @click="addTofolderCancle">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="'新增文件夹'" :visible.sync="newFolderOpen" width="500px" v-dialogDrag append-to-body>
      <el-form :model="newForm" :rules="newRules" label-width="80px">
        <el-form-item label="名称" prop="title">
          <el-input v-model="newForm.title" placeholder="请输入标题" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmAddNew">确 定</el-button>
        <el-button @click="cancleAddNew">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { moveFolder, addNewFolderAPI, addFileToFolder, giveTag, getAllInfo, searchDetail, createArchives, updateArchives, deleteArchives, getArchives, getArchivesPage, exportArchivesExcel, getFolderAPI } from "@/api/identity/archives";
export default {
  name: "Archives",
  components: {
  },
  data() {
    return {
      ocrText: '',
      detailOpen: false,
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 档案;列表
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
        title: null,
        date: [],
        refNum: null,
        status: null,
        userId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      options: [],
      props: { multiple: true },
      tagList: [],
      handleID: 0,
      tagChecked: '',
      userId: null,
      RuserId: null,
      folderList: {
      },
      allFiles: [],
      isAll: true,
      folderOpen: false,
      folderIndex: 0,
      newForm: {},
      newRules: {},
      newFolderOpen: false,
      tableData: [
      ],
      userName: '',
      role: undefined,
      openFolderId: '',
      isAdmin: false
    };
  },
  created() {
    this.isAdmin = !this.$auth.hasRole("admin_custom")
    // console.log(this.$store.state)
    this.userName = this.$store.state.user.nickname
    this.userId = this.$auth.hasRole("marker_custom") ? undefined : this.$store.state.user.id
    if (!this.isAdmin) {
      this.userId = undefined
    }
    console.log(this.userName, this.userId)
    this.role = this.$auth.hasRole("marker_custom") ? 1 : 0
    this.RuserId = this.$store.state.user.id
    this.getList();
    this.getFolderList()
  },
  methods: {
    cancleAddNew() {
      this.newFolderOpen = false
    },
    confirmAddNew() {
      console.log(this.newForm)
      const data = {
        title: this.newForm.title,
        userId: this.RuserId
      }
      if (this.newForm.title) {
        addNewFolderAPI(data).then(res => {
          if (res.code === 0) {
            this.$message.success('新增成功')
            this.getFolderList()
            this.newFolderOpen = false
            this.newForm = {}
          }
        }).catch(err => {
          this.$message.error(err)
        })
      } else {
        this.$message.error('请输入文件名')
      }
    },
    addNewFolder() {
      this.newFolderOpen = true
    },
    addTofolderCancle() {
      this.folderOpen = false
    },
    addTofolder() {
      const data = {
        archivesId: this.handleID,
        folderId: this.folderIndex,
        userId: this.RuserId
      }
      if (this.openFolderId) {
        const query = {
          folderId: this.openFolderId,
          targetFolderId: this.folderIndex
        }
        const dataM = [this.handleID]
        moveFolder(query, dataM).then(res => {
          if (res.code === 0) {
            this.$message.success('添加成功')
            this.folderOpen = false
            this.getFolderList()
          }
        })
      } else {
        addFileToFolder(data).then(res => {
          if (res.code === 0) {
            this.$message.success('添加成功')
            this.folderOpen = false
            this.getFolderList()
          }
        })
      }
    },
    handleAddFolder(row) {
      this.folderOpen = true
      this.handleID = row.id || row.archivesId
      // this.folderList.archivesExtendsRespFolderVOS.forEach(item => {
      //   item.label = item.title
      // })
    },
    checkFolder(files, folderId) {
      // console.log('checkfolder')
      if (files === 1) {
        this.list = this.folderList.archivesExtendsRespViewVOS
        this.total = this.folderList.archivesExtendsRespViewVOS.length
        this.isAll = false
      } else if (files === 0) {
        this.list = this.allFiles
        this.total = this.allFiles.length
        this.isAll = true
      }
      else {
        this.openFolderId = folderId
        this.list = files
        this.total = files.length
        this.isAll = false
      }
    },
    getFolderList() {
      getFolderAPI({ userId: this.RuserId }).then(res => {
        this.folderList = res.data
      })
    },
    handleItemChange(item) {
      // console.log(item)
      this.tagChecked = item.tagsText
    },
    labelFormat(label) {
      if (label == 0) {
        return '未标注'
      } else if (label == 1) {
        return '租户标注'
      } else if (label == 2) {
        return '标注员标注'
      } else {
        return '租户、标注员均标注'
      }
    },
    submitTag() {
      // 发起网络请求
      const params = {
        tagsText: this.tagChecked,
        archivesId: this.handleID,
        role: this.role,
        userId: this.RuserId,
        userName: this.userName
      }
      giveTag(params).then(res => {
        this.detailOpen = false
        this.$modal.msgSuccess("打标签成功")

      }).catch(err => {
        this.$modal.msgError("打标签失败");
      })
    },
    cancelTag() {
      this.detailOpen = false
    },
    /** 查询列表 */
    handleBiaozhu(row) {
      // console.log(row)
      this.detailOpen = true
      this.handleID = row.id
      searchDetail(row.id).then(res => {
        this.ocrText = res.data.ocrText
        const markerData = res.data.markerTagVOList.map(item => {
          return {
            name: item.markerUserName,
            tag: item.markerTag,
            identity: '标注员'
          }
        })
        let tenantData = {}
        if (res.data.tenantTagId) {
          tenantData = {
            name: res.data.tenantUserName,
            tag: res.data.tenantTag,
            identity: '用户'
          }
        }
        this.tableData = [...markerData, tenantData]
        // console.log(this.tableData)
      })

      getAllInfo().then(res => {
        console.log(res)
        this.tagList = res.data
      })


    },
    getList() {
      this.loading = true;
      const queryParams = {
        ...this.queryParams
      }
      // 执行查询
      getArchivesPage({ ...queryParams, userId: this.userId }).then(response => {
        this.list = response.data.list;
        this.allFiles = response.data.list;
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
        title: undefined,
        date: undefined,
        refNum: undefined,
        status: undefined,
        userId: undefined,
        ocrText: undefined,
        text: undefined
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
      this.title = "添加档案;";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getArchives(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改档案;";
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
          updateArchives(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交

        createArchives({ ...this.form, userId: this.RuserId, userName: this.userName }).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除档案;编号为"' + id + '"的数据项?').then(function () {
        return deleteArchives(id);
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
      this.$modal.confirm('是否确认导出所有档案;数据项?').then(() => {
        this.exportLoading = true;
        return exportArchivesExcel(params);
      }).then(response => {
        this.$download.excel(response, '档案;.xls');
        this.exportLoading = false;
      }).catch(() => { });
    }
  }
};
</script>

<style lang="scss" scoped>
.folder-wraper {
  margin-bottom: 20px;

  .el-button {
    margin-bottom: 10px;
  }
}

.tag-cotnainer {
  display: flex;
  justify-content: space-between;

  .left {
    width: 400px;
  }

  .right {
    width: 400px;

    .sql-container {
      .sql-title {
        font-size: 20px;
        font-weight: bold;
        margin-bottom: 10px;
        margin-top: 10px;
      }

      .radio-container {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;

        .el-radio {
          margin-bottom: 10px;
          margin-left: 0;
        }
      }
    }
  }
}
</style>
