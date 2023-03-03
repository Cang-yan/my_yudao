<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <!-- <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="关键词" prop="text">
        <el-input v-model="queryParams.text" placeholder="请输入关键词" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="用户id" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户id" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="0未标注 1有租户标注 2有标注员 3两个都有" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择0未标注 1有租户标注 2有标注员 3两个都有" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form> -->

    <div class="folder-wraper" v-if="isAdmin">
      <el-button @click="checkFolder(0)">全部
      </el-button>
      <el-button @click="checkFolder(1)">未分类
      </el-button>
      <el-button v-for="(item, index) in folderList.keywordsExtendsRespFolderVOS"
        @click="checkFolder(item.keywordsExtendsRespViewVOS, item.folderKeywordsId)" :key="index">{{ item.title }}
      </el-button>
    </div>


    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8" v-if="isAdmin">
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
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" :prop="difId" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="关键词" align="center" prop="text" />
      <el-table-column label="来源" align="center" prop="userName" />
      <!-- <el-table-column label="用户id" align="center" prop="userId" /> -->
      <el-table-column :label="'标注情况'" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ labelFormat(scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" v-if="isAdmin">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          <el-button size="mini" type="text" @click="handleBiaozhu(scope.row)">
            标注</el-button>
          <el-button size="mini" type="text" @click="handleAddFolder(scope.row)">添加到文件夹</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关键词" prop="text">
          <el-input v-model="form.text" placeholder="请输入关键词" />
        </el-form-item>
        <!-- <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item> -->
        <!-- <el-form-item label="0未标注 1有租户标注 2有标注员 3两个都有" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item> -->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="'查看详情'" :visible.sync="detailOpen" width="1000px" v-dialogDrag append-to-body>
      <div class="tag-cotnainer">
        <div class="left">
          {{ showKeywords }}
          <p style="font-size: 20px; font-weight: bold; margin-top: 20px;">已有标签</p>
          <el-table stripe :data="tableData" style="margin-top: 20px">
            <el-table-column label="标注人" prop="name"></el-table-column>
            <el-table-column label="身份" prop="identity"></el-table-column>
            <el-table-column label="标签" prop="tag"></el-table-column>
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
                  v-for="(item1, index1) in item.tagsMetaExtendsRespVOList" :key="index1">{{ item1.tagsText }}
                </el-radio>
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
        <el-radio :label="item.folderKeywordsId" :key="index"
          v-for="(item, index) in folderList.keywordsExtendsRespFolderVOS"
          style="margin-right: 10px; margin-bottom: 10px">
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
import { moveFolder, searchDetail, addNewFolderAPI, addFileToFolder, getFolderAPI, createKeywords, updateKeywords, deleteKeywords, getKeywords, getKeywordsPage, exportKeywordsExcel, giveTag } from "@/api/identity/keywords";
import { getAllInfo } from "@/api/identity/archives";
export default {
  name: "Keywords",
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
      // 关键词;列表
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
        userId: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      detailOpen: false,
      showKeywords: '',
      tagList: [],
      tagChecked: '',
      handleID: 0,
      userId: null,
      RuserId: null,
      folderOpen: false,
      folderIndex: 0,
      folderList: {},
      newFolderOpen: false,
      newForm: {},
      newRules: {},
      allFiles: [],
      difId: 'id',
      tableData: [

      ],
      role: undefined,
      openFolderId: '',
      isAdmin: false

    };
  },
  created() {
    this.isAdmin = !this.$auth.hasRole("admin_custom")
    this.userId = this.$auth.hasRole("marker_custom") ? null : this.$store.state.user.id
    this.role = this.$auth.hasRole("marker_custom") ? 1 : 0
    if (!this.isAdmin) {
      this.userId = undefined
    }
    this.userName = this.$store.state.user.nickname
    this.RuserId = this.$store.state.user.id
    this.getList();
    this.getFolderList()
  },
  methods: {
    checkFolder(files, folderId) {
      console.log(files)
      if (files === 1) {
        this.list = this.folderList.keywordsExtendsRespViewVOS
        this.total = this.folderList.keywordsExtendsRespViewVOS.length
        this.isAll = false
        this.difId = 'keywordsId'
      } else if (files === 0) {
        this.list = this.allFiles
        this.total = this.allFiles.length
        this.isAll = true
        this.difId = 'id'
      }
      else {
        this.openFolderId = folderId
        this.list = files
        this.total = files.length
        this.isAll = false
        this.difId = 'keywordsId'
      }
    },
    getFolderList() {
      getFolderAPI({ userId: this.RuserId }).then(res => {
        console.log(res)
        this.folderList = res.data
      })
    },
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
    addTofolderCancle() {
      this.folderOpen = false
    },
    addTofolder() {
      // console.log(this.openFolderId)
      const data = {
        keywordsId: this.handleID,
        folderKeywordsId: this.folderIndex,
        userId: this.RuserId
      }
      // console.log(data)
      if (this.openFolderId) {
        // 如果有id的话，证明是转移文件
        const query = {
          folderKeywordsId: this.openFolderId,
          targetFolderKeywordsId: this.folderIndex
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
        // 没有id的话就是新添加的
        addFileToFolder(data).then(res => {
          if (res.code === 0) {
            this.$message.success('添加成功')
            this.folderOpen = false
            this.getFolderList()
          }
        })
      }
    },
    addNewFolder() {
      this.newFolderOpen = true
    },
    handleAddFolder(row) {
      console.log(row)
      // this.openFolderId = row.id
      this.folderOpen = true
      this.handleID = row.id || row.keywordsId
      // console.log(this.handleID)
      // this.folderList.archivesExtendsRespFolderVOS.forEach(item => {
      //   item.label = item.title
      // })
    },
    cancelTag() {
      this.detailOpen = false
    },
    submitTag() {
      // 发起网络请求
      const params = {
        tagsText: this.tagChecked,
        keywordsId: this.handleID,
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
    handleBiaozhu(row) {
      // console.log(row)
      this.detailOpen = true
      this.handleID = row.id
      searchDetail(row.id).then(res => {
        console.log(res)
        const markerTagVOList = res.data.markerTagVOList ? res.data.markerTagVOList : []
        const markerData = markerTagVOList.map(item => {
          return {
            name: item.markerUserName,
            tag: item.markerTag,
            identity: '标注员'
          }
        })
        let tenantData = {}
        if (res.data.tenantTag) {
          tenantData = {
            name: res.data.tenantUserName,
            tag: res.data.tenantTag,
            identity: '用户'
          }
        }
        // console.log(tenantData)
        this.tableData = [...markerData, tenantData]
      })
      this.showKeywords = row.text

      getAllInfo().then(res => {
        console.log(res)
        this.tagList = res.data
      })

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
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getKeywordsPage({ ...this.queryParams, userId: this.userId }).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.allFiles = response.data.list;
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
        userId: undefined,
        status: undefined,
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
      this.title = "添加关键词";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      // console.log('触发修改操作')
      this.reset();
      const id = row.id;
      getKeywords(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改关键词";
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
          updateKeywords(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createKeywords({ ...this.form, userId: this.RuserId, status: 0, userName: this.userName }).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除关键词编号为"' + id + '"的数据项?').then(function () {
        return deleteKeywords(id);
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
      this.$modal.confirm('是否确认导出所有关键词数据项?').then(() => {
        this.exportLoading = true;
        return exportKeywordsExcel(params);
      }).then(response => {
        this.$download.excel(response, '关键词.xls');
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
