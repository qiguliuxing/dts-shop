<template>
  <div class="app-container">

    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.username" clearable class="filter-item" style="width: 200px;" placeholder="请输入用户名"/>
      <el-input v-model="listQuery.mobile" clearable class="filter-item" style="width: 200px;" placeholder="请输入手机号"/>
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button>
    </div>

    <!-- 查询结果 -->
    <el-table v-loading="listLoading" :data="list" size="small" element-loading-text="正在查询中。。。" border fit highlight-current-row>
      <el-table-column align="center" width="100px" label="用户ID" prop="id" sortable/>

      <el-table-column align="center" label="用户名" prop="username"/>

      <el-table-column align="center" label="手机号码" prop="mobile"/>

      <el-table-column align="center" label="性别" prop="gender">
        <template slot-scope="scope">
          <el-tag >{{ genderDic[scope.row.gender] }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="生日" prop="birthday"/>

      <el-table-column align="center" label="用户等级" prop="userLevel">
        <template slot-scope="scope">
          <el-tag >{{ levelDic[scope.row.userLevel] }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag>{{ statusDic[scope.row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-permission="['GET /admin/user/detailApprove']" v-if="scope.row.status==0 && scope.row.userLevel==2" type="primary" @click="handleDetail(scope.row)">推广代理</el-button>
          <el-button v-permission="['POST /admin/user/approveAgency']" v-else-if="scope.row.status==3" type="primary" size="mini" @click="handleApproveAgency(scope.row)">审批</el-button>
          <el-button v-permission="['GET /admin/user/detailApprove']" v-else type="info" size="mini" >非代理</el-button>
        </template>
      </el-table-column>

    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <!-- 详情对话框 -->
    <el-dialog :visible.sync="detailDialogVisible" title="代理详情" width="700">
      <el-form :data="agencyDetail" label-position="left">
        <el-form-item label="佣金比例(%)">
          <span>{{ agencyDetail.settlementRate }}</span>
        </el-form-item>
        <el-form-item label="推广二维码">
          <img :src="agencyDetail.shareUrl" width="300">
        </el-form-item>
      </el-form>
    </el-dialog>
    <!-- 代理审批 -->
    <el-dialog :visible.sync="approveDialogVisible" title="代理审批">
      <el-form ref="approveForm" :model="approveForm" status-icon label-position="left" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="佣金比例(%)" prop="settlementRate">
          <el-input v-model="approveForm.settlementRate"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove">确定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { fetchList, approveAgency, detailApprove } from '@/api/user'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination

export default {
  name: 'User',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        username: undefined,
        mobile: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      downloadLoading: false,
      genderDic: ['未知', '男', '女'],
      levelDic: ['普通用户', 'VIP用户', '代理'],
      statusDic: ['可用', '禁用', '注销', '代理申请'],
      detailDialogVisible: false,
      agencyDetail: {},
      approveDialogVisible: false,
      approveForm: {
        userId: undefined,
        settlementRate: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      fetchList(this.listQuery).then(response => {
        this.list = response.data.data.items
        this.total = response.data.data.total
        this.listLoading = false
      }).catch(() => {
        this.list = []
        this.total = 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleDetail(row) {
      this.agencyDetail = {
        shareUrl: undefined,
        settlementRate: undefined
      }
      detailApprove(row.id).then(response => {
        this.agencyDetail = response.data.data
      })
      this.detailDialogVisible = true
    },
    handleApproveAgency(row) {
      this.approveForm.userId = row.id

      this.approveDialogVisible = true
      this.$nextTick(() => {
        this.$refs['approveForm'].clearValidate()
      })
    },
    confirmApprove() {
      this.$refs['approveForm'].validate((valid) => {
        if (valid) {
          approveAgency(this.approveForm).then(response => {
            this.approveDialogVisible = false
            this.$notify.success({
              title: '成功',
              message: '审批成功'
            })
            this.getList()
          }).catch(response => {
            this.$notify.error({
              title: '审批失败',
              message: response.data.errmsg
            })
          })
        }
      })
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['用户名', '手机号码', '性别', '生日', '状态']
        const filterVal = ['username', 'mobile', 'gender', 'birthday', 'status']
        excel.export_json_to_excel2(tHeader, this.list, filterVal, '用户信息')
        this.downloadLoading = false
      })
    }
  }
}
</script>
