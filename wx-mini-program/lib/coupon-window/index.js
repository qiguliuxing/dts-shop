const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const user = require('../../utils/user.js');

var app = getApp();

Component({
  properties: {
    window:{
      type: Boolean,
      value: true,
    },
    couponList:{
      type:Array,
      value:[],
    }
  },
  data: {
  
  },
  attached: function () {
  
  },
  methods: {
    close:function(){
      this.triggerEvent('onColse');
    },
    getAllCoupon: function () {
      if (!app.globalData.hasLogin) {
        wx.navigateTo({
          url: "/pages/auth/login/login"
        });
      }
      util.request(api.CouponReceiveAll, null, 'POST').then(res => {
        if (res.errno === 0) {
          wx.showToast({
            title: "领取成功"
          });
          this.triggerEvent('onColse');
        }
        else {
          util.showErrorToast(res.errmsg);
        }
      })
    },
    
  }
})