import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// 样式文件
import './assets/styles/main.css'

// Vant 组件库
import { 
  Button, 
  NavBar, 
  Tabbar, 
  TabbarItem, 
  Cell, 
  CellGroup,
  Form,
  Field,
  Toast,
  Dialog,
  Loading,
  Empty,
  Grid,
  GridItem,
  Card,
  Tag,
  Search,
  Tabs,
  Tab,
  PullRefresh,
  List,
  Swipe,
  SwipeItem,
  Image,
  Icon,
  Popup,
  ActionSheet,
  DropdownMenu,
  DropdownItem,
  Slider,
  Switch,
  Checkbox,
  Radio,
  Rate,
  Uploader,
  Progress,
  Circle,
  Collapse,
  CollapseItem,
  Divider,
  NoticeBar,
  Sticky,
  SwipeCell,
  ShareSheet,
  ConfigProvider
} from 'vant'

const app = createApp(App)

// 注册 Vant 组件
app.use(Button)
app.use(NavBar)
app.use(Tabbar)
app.use(TabbarItem)
app.use(Cell)
app.use(CellGroup)
app.use(Form)
app.use(Field)
app.use(Toast)
app.use(Dialog)
app.use(Loading)
app.use(Empty)
app.use(Grid)
app.use(GridItem)
app.use(Card)
app.use(Tag)
app.use(Search)
app.use(Tabs)
app.use(Tab)
app.use(PullRefresh)
app.use(List)
app.use(Swipe)
app.use(SwipeItem)
app.use(Image)
app.use(Icon)
app.use(Popup)
app.use(ActionSheet)
app.use(DropdownMenu)
app.use(DropdownItem)
app.use(Slider)
app.use(Switch)
app.use(Checkbox)
app.use(Radio)
app.use(Rate)
app.use(Uploader)
app.use(Progress)
app.use(Circle)
app.use(Collapse)
app.use(CollapseItem)
app.use(Divider)
app.use(NoticeBar)
app.use(Sticky)
app.use(SwipeCell)
app.use(ShareSheet)
app.use(ConfigProvider)

app.use(createPinia())
app.use(router)

app.mount('#app')