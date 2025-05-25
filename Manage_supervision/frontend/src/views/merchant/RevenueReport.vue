<template>
  <div class="revenue-report">
    <h1 class="page-title">收入报表</h1>
    
    <el-row :gutter="20">
      <!-- 统计数据卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-card-content">
            <div class="stats-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">总收入</div>
              <div class="stats-value">¥{{ statsData.totalRevenue.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-card-content">
            <div class="stats-icon">
              <el-icon><Promotion /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">总订单数</div>
              <div class="stats-value">{{ statsData.totalOrders }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-card-content">
            <div class="stats-icon refund">
              <el-icon><RefreshLeft /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">退款金额</div>
              <div class="stats-value">¥{{ statsData.totalRefunds.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-card-content">
            <div class="stats-icon after-sale">
              <el-icon><Service /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">售后数量</div>
              <div class="stats-value">{{ statsData.totalAfterSales }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <!-- 收入趋势折线图 -->
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>收入趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart class="chart" :option="revenueChartOption" autoresize />
        </el-card>
      </el-col>
      
      <!-- 收入来源饼图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>收入构成</span>
            </div>
          </template>
          <v-chart class="chart" :option="revenuePieOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <!-- 订单与售后数量对比图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>订单与售后数量对比</span>
            </div>
          </template>
          <v-chart class="chart" :option="orderAfterSaleChartOption" autoresize />
        </el-card>
      </el-col>
      
      <!-- 订单状态分布 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>订单状态分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="orderStatusChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import axios from '../../utils/axios'
import { getMerchantRevenueReport } from '../../api/merchant'
import { 
  Money, 
  Promotion, 
  RefreshLeft, 
  Service,
  Goods
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

// 时间范围选择
const timeRange = ref('week')

// 统计数据
const statsData = ref({
  totalRevenue: 0,
  totalOrders: 0,
  totalRefunds: 0,
  totalAfterSales: 0
})

// 收入趋势数据
const revenueData = ref({
  dates: [],
  revenue: [],
  refunds: []
})

// 收入构成数据
const revenueSources = ref([
  { value: 0, name: '商品销售' },
  { value: 0, name: '增值服务' }
])

// 订单和售后数据
const orderAfterSaleData = ref({
  months: [],
  orders: [],
  afterSales: []
})

// 订单状态分布
const orderStatusData = ref([
  { value: 0, name: '待付款' },
  { value: 0, name: '待发货' },
  { value: 0, name: '待收货' },
  { value: 0, name: '已完成' },
  { value: 0, name: '已取消' }
])

// 收入趋势图配置
const revenueChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['收入', '退款']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: revenueData.value.dates
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '¥{value}'
    }
  },
  series: [
    {
      name: '收入',
      type: 'line',
      data: revenueData.value.revenue,
      itemStyle: {
        color: '#67C23A'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ]
        }
      },
      emphasis: {
        focus: 'series'
      }
    },
    {
      name: '退款',
      type: 'line',
      data: revenueData.value.refunds,
      itemStyle: {
        color: '#F56C6C'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 108, 108, 0.5)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
          ]
        }
      },
      emphasis: {
        focus: 'series'
      }
    }
  ]
}))

// 收入构成饼图配置
const revenuePieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    data: revenueSources.value.map(item => item.name)
  },
  series: [
    {
      name: '收入来源',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: revenueSources.value
    }
  ]
}))

// 订单与售后数量对比图配置
const orderAfterSaleChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['订单数量', '售后数量']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: orderAfterSaleData.value.months
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '订单数量',
      type: 'bar',
      data: orderAfterSaleData.value.orders,
      itemStyle: {
        color: '#409EFF'
      },
      emphasis: {
        focus: 'series'
      }
    },
    {
      name: '售后数量',
      type: 'bar',
      data: orderAfterSaleData.value.afterSales,
      itemStyle: {
        color: '#E6A23C'
      },
      emphasis: {
        focus: 'series'
      }
    }
  ]
}))

// 订单状态分布图配置
const orderStatusChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    data: orderStatusData.value.map(item => item.name)
  },
  series: [
    {
      name: '订单状态',
      type: 'pie',
      radius: '60%',
      data: orderStatusData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

// 加载报表数据
const loadReportData = async () => {
  try {
    // 调用后端API获取真实数据
    const { data } = await getMerchantRevenueReport(timeRange.value);
    if (!data) {
      throw new Error('获取数据失败');
    }
    
    // 设置统计数据
    statsData.value = {
      totalRevenue: data.totalRevenue || 0,
      totalOrders: data.totalOrders || 0,
      totalRefunds: data.totalRefunds || 0,
      totalAfterSales: data.totalAfterSales || 0
    };
    
    // 设置收入趋势数据
    revenueData.value = {
      dates: data.dates || [],
      revenue: data.revenue || [],
      refunds: data.refunds || []
    };
    
    // 设置收入构成数据
    if (data.revenueSources && data.revenueSources.length > 0) {
      revenueSources.value = data.revenueSources.map(item => ({
        value: item.value,
        name: item.name
      }));
    }
    
    // 设置订单和售后数据
    orderAfterSaleData.value = {
      months: data.months || [],
      orders: data.orders || [],
      afterSales: data.afterSales || []
    };
    
    // 设置订单状态分布
    if (data.orderStatus && data.orderStatus.length > 0) {
      orderStatusData.value = data.orderStatus.map(item => ({
        value: item.value,
        name: item.name
      }));
    }
    
  } catch (error) {
    console.error('加载报表数据失败:', error);
    ElMessage.error('加载报表数据失败');
  }
}

// 监听时间范围变化
watch(timeRange, () => {
  loadReportData()
})

// 组件挂载时加载数据
onMounted(() => {
  loadReportData()
})
</script>

<style scoped>
.revenue-report {
  padding: 24px;
}

.page-title {
  margin-bottom: 24px;
  font-size: 22px;
  font-weight: 500;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-card-content {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background-color: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 24px;
}

.stats-icon.refund {
  background-color: #F56C6C;
}

.stats-icon.after-sale {
  background-color: #E6A23C;
}

.stats-info {
  flex: 1;
}

.stats-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart {
  height: 340px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 1200px) {
  .el-col {
    width: 100%;
  }
}
</style> 