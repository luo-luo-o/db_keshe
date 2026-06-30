<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCircuit,
  createMeasurePoint,
  createTransformer,
  deleteCircuit,
  deleteMeasurePoint,
  deleteTransformer,
  updateCircuit,
  updateMeasurePoint,
  updateTransformer,
} from '../api/dashboard'
import type { AuthSession } from '../types/auth'
import {
  phaseCodeOptions,
  pointGroupLabels,
  type CircuitDirection,
  type CircuitOptionResponse,
  type CreateCircuitPayload,
  type CreateMeasurePointPayload,
  type CreateTransformerPayload,
  type DeviceStatus,
  type MeasurePointOptionResponse,
  type MeasurePointStatus,
  type PhaseCode,
  type PointGroup,
  type TransformerOptionResponse,
} from '../types/dashboard'

interface ManagedPointRow extends MeasurePointOptionResponse {
  circuitId?: number
  circuitName?: string
}

type PointScope = 'ALL' | 'DIRECT' | number

type TransformerDialogMode = 'create' | 'edit'
type CircuitDialogMode = 'create' | 'edit'
type PointDialogMode = 'create' | 'edit'

const props = defineProps<{
  modelValue: boolean
  session: AuthSession
  transformers: TransformerOptionResponse[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  changed: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const transformerDialogVisible = ref(false)
const circuitDialogVisible = ref(false)
const pointDialogVisible = ref(false)
const transformerDialogMode = ref<TransformerDialogMode>('create')
const circuitDialogMode = ref<CircuitDialogMode>('create')
const pointDialogMode = ref<PointDialogMode>('create')
const isSubmitting = ref(false)
const selectedTransformerId = ref<number>()
const selectedCircuitId = ref<number>()
const pointScope = ref<PointScope>('ALL')
const allowTransformerAutoSelect = ref(true)
const editingTransformerId = ref<number>()
const editingCircuitId = ref<number>()
const editingPointId = ref<number>()

const transformerStatusOptions: Array<{ label: string; value: DeviceStatus }> = [
  { label: '正常', value: 0 },
  { label: '告警', value: 1 },
  { label: '停运', value: 2 },
]

const pointStatusOptions: Array<{ label: string; value: MeasurePointStatus }> = [
  { label: '启用', value: 0 },
  { label: '停用', value: 1 },
]

const directionOptions: Array<{ label: string; value: CircuitDirection }> = [
  { label: '进线', value: 'INCOMING' },
  { label: '出线', value: 'OUTGOING' },
]

const pointGroupOptions: Array<{ label: string; value: PointGroup }> = [
  { label: pointGroupLabels.ELECTRIC_IN, value: 'ELECTRIC_IN' },
  { label: pointGroupLabels.ELECTRIC_OUT, value: 'ELECTRIC_OUT' },
  { label: pointGroupLabels.TRANSFORMER, value: 'TRANSFORMER' },
  { label: pointGroupLabels.CABINET, value: 'CABINET' },
]

const measureTypeOptions = [
  'VOLTAGE',
  'CURRENT',
  'POWER_FACTOR',
  'ENERGY',
  'OIL_TEMP',
  'SWITCH_STATUS',
  'FUSE_STATUS',
  'CABINET_TEMP',
  'CABINET_HUMIDITY',
  'SMOKE_STATUS',
  'DOOR_STATUS',
  'FREQUENCY',
]

const transformerForm = reactive({
  transformerCode: '',
  transformerName: '',
  ratedCapacityKva: undefined as number | undefined,
  ratedVoltageRatio: '',
  commissionDate: '',
  manufacturer: '',
  oilLevel: undefined as number | undefined,
  location: '',
  status: 0 as DeviceStatus,
})

const circuitForm = reactive({
  circuitCode: '',
  circuitName: '',
  direction: 'OUTGOING' as CircuitDirection,
  ratedVoltageKv: undefined as number | undefined,
  ratedCurrentA: undefined as number | undefined,
  status: 0 as DeviceStatus,
})

const pointForm = reactive({
  circuitId: undefined as number | undefined,
  pointCode: '',
  pointName: '',
  pointGroup: 'TRANSFORMER' as PointGroup,
  measureType: 'VOLTAGE',
  phaseCode: 'NONE' as PhaseCode,
  unit: '',
  minLimit: undefined as number | undefined,
  maxLimit: undefined as number | undefined,
  rateLimit: undefined as number | undefined,
  status: 0 as MeasurePointStatus,
})

const selectedTransformer = computed(() =>
  props.transformers.find((transformer) => transformer.transformerId === selectedTransformerId.value) ?? null,
)

const selectedTransformerCircuits = computed(() => selectedTransformer.value?.circuits ?? [])

const pointScopeOptions = computed(() => [
  { label: '全部测点', value: 'ALL' as PointScope },
  { label: '直属测点', value: 'DIRECT' as PointScope },
  ...selectedTransformerCircuits.value.map((circuit) => ({
    label: `${circuit.circuitName} (${directionLabel(circuit.direction)})`,
    value: circuit.circuitId as PointScope,
  })),
])

const managedPoints = computed<ManagedPointRow[]>(() => {
  if (!selectedTransformer.value) {
    return []
  }

  const directPoints = selectedTransformer.value.points.map((point) => ({ ...point }))
  const circuitPoints = selectedTransformer.value.circuits.flatMap((circuit) =>
    circuit.points.map((point) => ({
      ...point,
      circuitId: circuit.circuitId,
      circuitName: circuit.circuitName,
    })),
  )

  if (pointScope.value === 'DIRECT') {
    return directPoints
  }

  if (typeof pointScope.value === 'number') {
    return circuitPoints.filter((point) => point.circuitId === pointScope.value)
  }

  return [...directPoints, ...circuitPoints]
})

watch(
  () => props.transformers,
  (transformers) => {
    if (transformers.length === 0) {
      selectedTransformerId.value = undefined
      selectedCircuitId.value = undefined
      pointScope.value = 'ALL'
      circuitDialogVisible.value = false
      pointDialogVisible.value = false
      allowTransformerAutoSelect.value = true
      return
    }

    const hasSelectedTransformer =
      selectedTransformerId.value !== undefined
      && transformers.some((item) => item.transformerId === selectedTransformerId.value)

    if (!hasSelectedTransformer && allowTransformerAutoSelect.value) {
      selectedTransformerId.value = transformers[0].transformerId
    } else if (!hasSelectedTransformer) {
      selectedTransformerId.value = undefined
      selectedCircuitId.value = undefined
      pointScope.value = 'ALL'
      circuitDialogVisible.value = false
      pointDialogVisible.value = false
    }

    const currentCircuits = transformers.find((item) => item.transformerId === selectedTransformerId.value)?.circuits ?? []
    if (selectedCircuitId.value && !currentCircuits.some((item) => item.circuitId === selectedCircuitId.value)) {
      selectedCircuitId.value = undefined
      pointDialogVisible.value = false
    }

    if (typeof pointScope.value === 'number' && !currentCircuits.some((item) => item.circuitId === pointScope.value)) {
      pointScope.value = 'ALL'
    }
  },
  { immediate: true, deep: true },
)

watch(visible, (isOpen) => {
  if (isOpen && !selectedTransformerId.value && props.transformers.length > 0) {
    allowTransformerAutoSelect.value = true
    selectedTransformerId.value = props.transformers[0].transformerId
  }
})

watch(selectedTransformerId, () => {
  selectedCircuitId.value = undefined
  pointScope.value = 'ALL'
})

function selectTransformer(transformerId: number) {
  allowTransformerAutoSelect.value = true
  selectedTransformerId.value = transformerId
}

function selectCircuit(circuitId: number) {
  selectedCircuitId.value = circuitId
}

function openCreateTransformer() {
  transformerDialogMode.value = 'create'
  editingTransformerId.value = undefined
  Object.assign(transformerForm, {
    transformerCode: '',
    transformerName: '',
    ratedCapacityKva: undefined,
    ratedVoltageRatio: '',
    commissionDate: '',
    manufacturer: '',
    oilLevel: undefined,
    location: '',
    status: 0,
  })
  transformerDialogVisible.value = true
}

function openEditTransformer(transformer: TransformerOptionResponse) {
  transformerDialogMode.value = 'edit'
  editingTransformerId.value = transformer.transformerId
  Object.assign(transformerForm, {
    transformerCode: transformer.transformerCode,
    transformerName: transformer.transformerName,
    ratedCapacityKva: transformer.ratedCapacityKva,
    ratedVoltageRatio: transformer.ratedVoltageRatio ?? '',
    commissionDate: transformer.commissionDate ?? '',
    manufacturer: transformer.manufacturer ?? '',
    oilLevel: transformer.oilLevel,
    location: transformer.location ?? '',
    status: transformer.status,
  })
  transformerDialogVisible.value = true
}

function openCreateCircuit() {
  if (!selectedTransformer.value) {
    return
  }

  circuitDialogMode.value = 'create'
  editingCircuitId.value = undefined
  Object.assign(circuitForm, {
    circuitCode: '',
    circuitName: '',
    direction: 'OUTGOING',
    ratedVoltageKv: undefined,
    ratedCurrentA: undefined,
    status: 0,
  })
  circuitDialogVisible.value = true
}

function openEditCircuit(circuit: CircuitOptionResponse) {
  circuitDialogMode.value = 'edit'
  editingCircuitId.value = circuit.circuitId
  selectedCircuitId.value = circuit.circuitId
  Object.assign(circuitForm, {
    circuitCode: circuit.circuitCode,
    circuitName: circuit.circuitName,
    direction: circuit.direction,
    ratedVoltageKv: circuit.ratedVoltageKv,
    ratedCurrentA: circuit.ratedCurrentA,
    status: circuit.status,
  })
  circuitDialogVisible.value = true
}

function openCreatePoint() {
  if (!selectedTransformer.value) {
    return
  }

  pointDialogMode.value = 'create'
  editingPointId.value = undefined
  Object.assign(pointForm, {
    circuitId: typeof pointScope.value === 'number' ? pointScope.value : selectedCircuitId.value,
    pointCode: '',
    pointName: '',
    pointGroup: typeof pointScope.value === 'number' ? inferPointGroupFromCircuitId(pointScope.value) : 'TRANSFORMER',
    measureType: 'VOLTAGE',
    phaseCode: 'NONE',
    unit: '',
    minLimit: undefined,
    maxLimit: undefined,
    rateLimit: undefined,
    status: 0,
  })
  pointDialogVisible.value = true
}

function openEditPoint(point: ManagedPointRow) {
  pointDialogMode.value = 'edit'
  editingPointId.value = point.id
  Object.assign(pointForm, {
    circuitId: point.circuitId,
    pointCode: point.pointCode,
    pointName: point.pointName,
    pointGroup: point.pointGroup,
    measureType: point.measureType,
    phaseCode: point.phaseCode,
    unit: point.unit ?? '',
    minLimit: point.minLimit,
    maxLimit: point.maxLimit,
    rateLimit: point.rateLimit,
    status: point.status,
  })
  pointDialogVisible.value = true
}

async function submitTransformer() {
  isSubmitting.value = true

  try {
    const payload = buildTransformerPayload()
    if (transformerDialogMode.value === 'create') {
      await createTransformer(props.session, payload)
      ElMessage.success('已新增箱变')
    } else if (editingTransformerId.value) {
      await updateTransformer(props.session, editingTransformerId.value, payload)
      ElMessage.success('已更新箱变')
    }
    transformerDialogVisible.value = false
    emit('changed')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    isSubmitting.value = false
  }
}

async function submitCircuit() {
  if (!selectedTransformer.value) {
    return
  }

  isSubmitting.value = true
  try {
    const payload = buildCircuitPayload()
    if (circuitDialogMode.value === 'create') {
      await createCircuit(props.session, selectedTransformer.value.transformerId, payload)
      ElMessage.success('已新增回路')
    } else if (editingCircuitId.value) {
      await updateCircuit(props.session, editingCircuitId.value, payload)
      ElMessage.success('已更新回路')
    }
    circuitDialogVisible.value = false
    emit('changed')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    isSubmitting.value = false
  }
}

async function submitPoint() {
  if (!selectedTransformer.value) {
    return
  }

  isSubmitting.value = true
  try {
    const payload = buildPointPayload()
    if (pointDialogMode.value === 'create') {
      await createMeasurePoint(props.session, selectedTransformer.value.transformerId, payload)
      ElMessage.success('已新增测点')
    } else if (editingPointId.value) {
      await updateMeasurePoint(props.session, editingPointId.value, payload)
      ElMessage.success('已更新测点')
    }
    pointDialogVisible.value = false
    emit('changed')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    isSubmitting.value = false
  }
}

async function confirmDeleteTransformer(transformer: TransformerOptionResponse) {
  try {
    await ElMessageBox.confirm(
      `将删除箱变“${transformer.transformerName}”，删除后不可恢复，历史数据保留。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteTransformer(props.session, transformer.transformerId)
    if (selectedTransformerId.value === transformer.transformerId) {
      allowTransformerAutoSelect.value = false
      selectedTransformerId.value = undefined
      selectedCircuitId.value = undefined
      pointScope.value = 'ALL'
      circuitDialogVisible.value = false
      pointDialogVisible.value = false
    }
    ElMessage.success('已删除箱变')
    emit('changed')
  } catch (error) {
    if (!isCancel(error)) {
      ElMessage.error(getErrorMessage(error))
    }
  }
}

async function confirmDeleteCircuit(circuit: CircuitOptionResponse) {
  try {
    await ElMessageBox.confirm(
      `将删除回路“${circuit.circuitName}”，删除后不可恢复，历史数据保留。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteCircuit(props.session, circuit.circuitId)
    if (selectedCircuitId.value === circuit.circuitId) {
      selectedCircuitId.value = undefined
      pointDialogVisible.value = false
    }
    if (pointScope.value === circuit.circuitId) {
      pointScope.value = 'ALL'
    }
    ElMessage.success('已删除回路')
    emit('changed')
  } catch (error) {
    if (!isCancel(error)) {
      ElMessage.error(getErrorMessage(error))
    }
  }
}

async function confirmDeletePoint(point: ManagedPointRow) {
  try {
    await ElMessageBox.confirm(
      `将删除测点“${point.pointName}”，删除后不可恢复，历史数据保留。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteMeasurePoint(props.session, point.id)
    if (editingPointId.value === point.id) {
      pointDialogVisible.value = false
    }
    ElMessage.success('已删除测点')
    emit('changed')
  } catch (error) {
    if (!isCancel(error)) {
      ElMessage.error(getErrorMessage(error))
    }
  }
}

function buildTransformerPayload(): CreateTransformerPayload {
  return {
    transformerCode: transformerForm.transformerCode.trim(),
    transformerName: transformerForm.transformerName.trim(),
    ratedCapacityKva: transformerForm.ratedCapacityKva,
    ratedVoltageRatio: trimOptional(transformerForm.ratedVoltageRatio),
    commissionDate: trimOptional(transformerForm.commissionDate),
    manufacturer: trimOptional(transformerForm.manufacturer),
    oilLevel: transformerForm.oilLevel,
    location: trimOptional(transformerForm.location),
    status: transformerForm.status,
  }
}

function buildCircuitPayload(): CreateCircuitPayload {
  return {
    circuitCode: circuitForm.circuitCode.trim(),
    circuitName: circuitForm.circuitName.trim(),
    direction: circuitForm.direction,
    ratedVoltageKv: circuitForm.ratedVoltageKv,
    ratedCurrentA: circuitForm.ratedCurrentA,
    status: circuitForm.status,
  }
}

function buildPointPayload(): CreateMeasurePointPayload {
  return {
    circuitId: pointForm.circuitId,
    pointCode: pointForm.pointCode.trim(),
    pointName: pointForm.pointName.trim(),
    pointGroup: pointForm.pointGroup,
    measureType: pointForm.measureType,
    phaseCode: pointForm.phaseCode,
    unit: trimOptional(pointForm.unit),
    minLimit: pointForm.minLimit,
    maxLimit: pointForm.maxLimit,
    rateLimit: pointForm.rateLimit,
    status: pointForm.status,
  }
}

function inferPointGroupFromCircuitId(circuitId: number): PointGroup {
  const circuit = selectedTransformer.value?.circuits.find((item) => item.circuitId === circuitId)
  return circuit?.direction === 'INCOMING' ? 'ELECTRIC_IN' : 'ELECTRIC_OUT'
}

function directionLabel(direction: CircuitDirection) {
  return direction === 'INCOMING' ? '进线' : '出线'
}

function statusLabel(status: DeviceStatus) {
  if (status === 0) {
    return '正常'
  }
  if (status === 1) {
    return '告警'
  }
  return '停运'
}

function statusTagType(status: DeviceStatus) {
  if (status === 0) {
    return 'success'
  }
  if (status === 1) {
    return 'danger'
  }
  return 'warning'
}

function pointStatusLabel(status: MeasurePointStatus) {
  return status === 0 ? '启用' : '停用'
}

function pointStatusTagType(status: MeasurePointStatus) {
  return status === 0 ? 'success' : 'info'
}

function transformerRowClass({ row }: { row: TransformerOptionResponse }) {
  return row.transformerId === selectedTransformerId.value ? 'is-selected-row' : ''
}

function circuitRowClass({ row }: { row: CircuitOptionResponse }) {
  return row.circuitId === selectedCircuitId.value ? 'is-selected-row' : ''
}

function pointGroupLabel(point: ManagedPointRow) {
  return pointGroupLabels[point.pointGroup]
}

function trimOptional(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : undefined
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '操作失败'
}

function isCancel(error: unknown) {
  return error === 'cancel' || error === 'close'
}
</script>

<template>
  <el-dialog v-model="visible" title="设备管理" width="1360px" top="4vh" destroy-on-close>
    <div class="device-manager">
      <section class="manager-section">
        <div class="section-header">
          <div>
            <h3>箱变</h3>
            <p>新增、编辑、删除箱变，并维护三态状态。</p>
          </div>
          <el-button type="primary" @click="openCreateTransformer">新增箱变</el-button>
        </div>
        <el-table
          :data="transformers"
          border
          stripe
          max-height="260"
          :row-class-name="transformerRowClass"
          @row-click="(row: TransformerOptionResponse) => selectTransformer(row.transformerId)"
        >
          <el-table-column prop="transformerCode" label="编码" min-width="130" />
          <el-table-column prop="transformerName" label="箱变" min-width="160" />
          <el-table-column prop="location" label="位置" min-width="180" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button size="small" @click.stop="openEditTransformer(row)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="confirmDeleteTransformer(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="manager-section">
        <div class="section-header">
          <div>
            <h3>回路</h3>
            <p v-if="selectedTransformer">当前箱变：{{ selectedTransformer.transformerName }}</p>
            <p v-else>请先选中一个箱变。</p>
          </div>
          <el-button type="primary" :disabled="!selectedTransformer" @click="openCreateCircuit">新增回路</el-button>
        </div>
        <el-table
          :data="selectedTransformerCircuits"
          border
          stripe
          max-height="260"
          :row-class-name="circuitRowClass"
          @row-click="(row: CircuitOptionResponse) => selectCircuit(row.circuitId)"
        >
          <el-table-column prop="circuitCode" label="编码" min-width="120" />
          <el-table-column prop="circuitName" label="回路" min-width="150" />
          <el-table-column label="方向" width="100">
            <template #default="{ row }">{{ directionLabel(row.direction) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button size="small" @click.stop="openEditCircuit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="confirmDeleteCircuit(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="manager-section">
        <div class="section-header section-header-points">
          <div>
            <h3>测点</h3>
            <p v-if="selectedTransformer">支持按直属测点、指定回路或全部测点查看。</p>
            <p v-else>请先选中一个箱变。</p>
          </div>
          <div class="points-toolbar">
            <el-select v-model="pointScope" style="width: 220px" :disabled="!selectedTransformer">
              <el-option
                v-for="option in pointScopeOptions"
                :key="String(option.value)"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <el-button type="primary" :disabled="!selectedTransformer" @click="openCreatePoint">新增测点</el-button>
          </div>
        </div>
        <el-table :data="managedPoints" border stripe max-height="320">
          <el-table-column prop="pointCode" label="编码" min-width="150" />
          <el-table-column prop="pointName" label="测点" min-width="160" />
          <el-table-column label="挂载位置" min-width="140">
            <template #default="{ row }">{{ row.circuitName ?? '直属箱变' }}</template>
          </el-table-column>
          <el-table-column label="分组" min-width="120">
            <template #default="{ row }">{{ pointGroupLabel(row) }}</template>
          </el-table-column>
          <el-table-column prop="measureType" label="测量类型" min-width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="pointStatusTagType(row.status)">{{ pointStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button size="small" @click.stop="openEditPoint(row)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="confirmDeletePoint(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </el-dialog>

  <el-dialog v-model="transformerDialogVisible" :title="transformerDialogMode === 'create' ? '新增箱变' : '编辑箱变'" width="640px">
    <el-form :model="transformerForm" label-position="top" class="manager-form-grid">
      <el-form-item label="箱变编码">
        <el-input v-model="transformerForm.transformerCode" />
      </el-form-item>
      <el-form-item label="箱变名称">
        <el-input v-model="transformerForm.transformerName" />
      </el-form-item>
      <el-form-item label="额定容量(kVA)">
        <el-input-number v-model="transformerForm.ratedCapacityKva" :min="0" :precision="3" style="width: 100%" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="transformerForm.status">
          <el-option v-for="option in transformerStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="电压比">
        <el-input v-model="transformerForm.ratedVoltageRatio" placeholder="如 10/0.4kV" />
      </el-form-item>
      <el-form-item label="投运日期">
        <el-date-picker v-model="transformerForm.commissionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
      </el-form-item>
      <el-form-item label="厂商">
        <el-input v-model="transformerForm.manufacturer" />
      </el-form-item>
      <el-form-item label="油位">
        <el-input-number v-model="transformerForm.oilLevel" :precision="3" style="width: 100%" />
      </el-form-item>
      <el-form-item label="位置" class="full-span">
        <el-input v-model="transformerForm.location" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="transformerDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="isSubmitting" @click="submitTransformer">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="circuitDialogVisible" :title="circuitDialogMode === 'create' ? '新增回路' : '编辑回路'" width="620px">
    <el-form :model="circuitForm" label-position="top" class="manager-form-grid">
      <el-form-item label="回路编码">
        <el-input v-model="circuitForm.circuitCode" />
      </el-form-item>
      <el-form-item label="回路名称">
        <el-input v-model="circuitForm.circuitName" />
      </el-form-item>
      <el-form-item label="方向">
        <el-select v-model="circuitForm.direction">
          <el-option v-for="option in directionOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="circuitForm.status">
          <el-option v-for="option in transformerStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="额定电压(kV)">
        <el-input-number v-model="circuitForm.ratedVoltageKv" :min="0" :precision="3" style="width: 100%" />
      </el-form-item>
      <el-form-item label="额定电流(A)">
        <el-input-number v-model="circuitForm.ratedCurrentA" :min="0" :precision="3" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="circuitDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="isSubmitting" @click="submitCircuit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="pointDialogVisible" :title="pointDialogMode === 'create' ? '新增测点' : '编辑测点'" width="760px">
    <el-form :model="pointForm" label-position="top" class="manager-form-grid point-form-grid">
      <el-form-item label="挂载回路">
        <el-select v-model="pointForm.circuitId" clearable placeholder="直属箱变">
          <el-option label="直属箱变" :value="undefined" />
          <el-option
            v-for="circuit in selectedTransformerCircuits"
            :key="circuit.circuitId"
            :label="`${circuit.circuitName} (${directionLabel(circuit.direction)})`"
            :value="circuit.circuitId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="测点编码">
        <el-input v-model="pointForm.pointCode" />
      </el-form-item>
      <el-form-item label="测点名称">
        <el-input v-model="pointForm.pointName" />
      </el-form-item>
      <el-form-item label="启停状态">
        <el-select v-model="pointForm.status">
          <el-option v-for="option in pointStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="测点分组">
        <el-select v-model="pointForm.pointGroup">
          <el-option v-for="option in pointGroupOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="测量类型">
        <el-select v-model="pointForm.measureType" filterable>
          <el-option v-for="option in measureTypeOptions" :key="option" :label="option" :value="option" />
        </el-select>
      </el-form-item>
      <el-form-item label="相位">
        <el-select v-model="pointForm.phaseCode">
          <el-option v-for="option in phaseCodeOptions" :key="option" :label="option" :value="option" />
        </el-select>
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="pointForm.unit" />
      </el-form-item>
      <el-form-item label="最小阈值">
        <el-input-number v-model="pointForm.minLimit" :precision="4" style="width: 100%" />
      </el-form-item>
      <el-form-item label="最大阈值">
        <el-input-number v-model="pointForm.maxLimit" :precision="4" style="width: 100%" />
      </el-form-item>
      <el-form-item label="速率阈值">
        <el-input-number v-model="pointForm.rateLimit" :precision="4" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pointDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="isSubmitting" @click="submitPoint">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.device-manager {
  display: grid;
  gap: 18px;
}

.manager-section {
  border: 1px solid #d9e2ec;
  border-radius: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.96);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
}

.section-header p {
  margin: 4px 0 0;
  color: #64748b;
}

.section-header-points {
  align-items: flex-end;
}

.points-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.manager-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.point-form-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.full-span {
  grid-column: 1 / -1;
}

:deep(.is-selected-row) {
  --el-table-tr-bg-color: rgba(59, 130, 246, 0.08);
}

@media (max-width: 900px) {
  .section-header,
  .section-header-points,
  .points-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .manager-form-grid,
  .point-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>