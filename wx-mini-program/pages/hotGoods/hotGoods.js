var util = require('../../utils/util.js');
var api = require('../../config/api.js');
var app = getApp();

Page({
  data: {
    bannerInfo: {
      'imgUrl': 'http://yanxuan.nosdn.127.net/8976116db321744084774643a933c5ce.png',
      'name': '大家都在买的'
    },
    categoryFilter: false,
    filterCategory: [],
    goodsList: [],
    categoryId: 0,
    currentSortType: 'default',
    currentSort: 'browse',
    currentSortOrder: 'desc',
    page: 1,
    size: 10,
    totalPages: 1
  },
  getCategoryList: function() {
    var that = this;

    util.request(api.GoodsFilter, {
        isHot: 1
      })
      .then(function(res) {
        if (res.errno === 0) {
          that.setData({
            filterCategory: res.data.filterCategoryList,
          });
        }
      });
  },
  getGoodsList: function() {
    var that = this;

    util.request(api.GoodsList, {
        isHot: true,
        page: that.data.page,
        size: that.data.size,
        order: that.data.currentSortOrder,
        sort: that.data.currentSort,
        categoryId: that.data.categoryId
      })
      .then(function(res) {
        if (res.errno === 0) {
          that.setData({
            goodsList: that.data.goodsList.concat(res.data.goodsList),
            filterCategory: res.data.filterCategoryList,
            totalPages: res.data.totalPages
          });
        }
      });

  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.getGoodsList();
  },
  onReady: function() {
    // 页面渲染完成
  },
  onShow: function() {
    // 页面显示

  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  },
  openSortFilter: function(event) {
    let currentId = event.currentTarget.id;
    let tmpSortOrder = 'asc';
    switch (currentId) {
      case 'categoryFilter':
        this.setData({
          categoryFilter: !this.data.categoryFilter,
          currentSortType: 'category',
          currentSort: 'add_time',
          currentSortOrder: 'desc'
        });
        break;
      case 'priceSort':
        if (this.data.currentSortOrder == 'asc') {
          tmpSortOrder = 'desc';
        }
        this.setData({
          currentSortType: 'price',
          currentSort: 'retail_price',
          currentSortOrder: tmpSortOrder,
          categoryFilter: false
        });

        this.getGoodsList();
        break;
      case 'salesSort':
        if (this.data.currentSortOrder == 'asc') {
          tmpSortOrder = 'desc';
        }
        this.setData({
          currentSortType: 'sales',
          currentSort: 'sales',
          currentSortOrder: tmpSortOrder,
          categoryFilter: false
        });
        this.getGoodsList();
        break;
      default:
        //综合排序
        this.setData({
          currentSortType: 'default',
          currentSort: 'add_time',
          currentSortOrder: 'desc',
          categoryFilter: false,
          categoryId: 0,
        });
        this.getGoodsList();
    }
  },
  selectCategory: function(event) {
    let currentIndex = event.target.dataset.categoryIndex;
    this.setData({
      'categoryFilter': false,
      'categoryId': this.data.filterCategory[currentIndex].id
    });
    this.getGoodsList();
  }, 
  onReachBottom:function() {
	if (this.data.totalPages > this.data.page) {
	    this.setData({
		  page: this.data.page + 1
	    });
	} else {
		wx.showToast({
		  title: '已经到底了!',
		  icon: 'none',
		  duration: 2000
		});
		return false;
	}
	 this.getGoodsList();
  }
})