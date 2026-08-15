// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

import store from "../store";
import Cache from '../utils/cache';
import { Debounce } from '@/utils/validate.js'
// #ifdef H5 || APP-PLUS
import { isWeixin } from "../utils";
import auth from './wechat';
// #endif

import { LOGIN_STATUS, USER_INFO, EXPIRES_TIME, STATE_R_KEY, BACK_URL, GLOBAL_DATA} from './../config/cache';
import {globalConfigApi} from "../api/public";
import util from "../utils/util";

function prePage(){
	let pages = getCurrentPages();
	let prePage = pages[pages.length - 1];
	return prePage.$page.fullPath;
}

export const toLogin = Debounce(_toLogin,800)

export function _toLogin(push, pathLogin) {
	// 公众号登录方式(单选),1微信授权，2手机号登录/
	let publicLoginType = Cache.get('publicLoginType');

	let path = prePage();
	let login_back_url = Cache.get(BACK_URL);
	// #ifdef H5
	path = location.href;
	path = location.pathname + location.search;
	// #endif
	if(!pathLogin){
		pathLogin = '/page/users/login/index'
		Cache.set(BACK_URL,path);
	}
		
	// #ifdef H5
	if (isWeixin() && publicLoginType ==1) {
		let urlData = location.pathname + location.search
		if (urlData.indexOf('?') !== -1) {
			urlData += '&go_longin=1';
		} else {
			urlData += '?go_longin=1';
		}
		if (!Cache.has('snsapiKey')) {
			auth.oAuth('snsapi_base', urlData);
		} else {
			uni.navigateTo({
				url: '/pages/users/wechat_login/index'
			})
		}
	} else {
		uni.navigateTo({
			url: '/pages/users/login/index'
		})
	}
	// #endif
	
	if (['pages/user/index','/pages/user/index','/pages/order_addcart/order_addcart'].indexOf(login_back_url) == -1) {
		// #ifdef MP
		uni.navigateTo({
			 url: '/pages/users/wechat_login/index'
		})
		// #endif
		// #ifdef APP-PLUS
			uni.showModal({
			    title: '登录提示',
			    content: '登录以后可体验商城完整功能',
				cancelColor: '#000000',
				showCancel: false, // 是否显示取消按钮，默认为 true
				confirmColor: '#526BB1',
			    success: function (res) {
			        if (res.confirm) {
			           uni.navigateTo({
			           	url: '/pages/users/login/index'
			           })
			        } else if (res.cancel) {
			            uni.navigateTo({
			            	url: '/pages/index/index'
			            })
			        }
			    }
			});
		// #endif
	}
}


export function checkLogin()
{
	let token = Cache.get(LOGIN_STATUS);
	let expiresTime = Cache.get(EXPIRES_TIME);
	let newTime = Math.round(new Date() / 1000);
	if (expiresTime < newTime || !token){
		Cache.clear(LOGIN_STATUS);
		Cache.clear(EXPIRES_TIME);
		Cache.clear(USER_INFO);
		Cache.clear(STATE_R_KEY);
		return false;
	}else{
		store.commit('UPDATE_LOGIN',token);
		let userInfo = Cache.get(USER_INFO,true);
		if(userInfo){
			store.commit('UPDATE_USERINFO',userInfo);
		}
		return true;
	}

}

export async function checkAutoLogin()
{  
   try {
     // 1. 尝试获取并清理ID参数
      const rawId = getSafeUrlParam('id');
      console.log('原始ID参数:', rawId); // 调试用
    
      // 2. 如果有ID参数，尝试自动注册
      if (rawId) {
        const registerSuccess = await autoRegister(rawId);
        if (registerSuccess) return true;
      }
 
     return false;
   } catch (e) {
     console.error('[登录检查异常]', e);
     return false;
   }
}

/**
 * 自动注册
 */
async function autoRegister(rawId) {
  // 1. 清理参数
  const id = String(rawId).trim();
  if (!id) {
    console.error('ID参数无效');
    return false;
  }
  
  //适配多端
  const baseUrl = process.env.NODE_ENV === 'development' 
    ? 'http://localhost:8081'  // 开发环境
	 : ''; 

  //调用注册接口
  try {
    const res = await uni.request({
      url: `${baseUrl}/api/front/login/mobile/password`,  // 注意是反引号
      method: 'POST',
    data: {
          account: id,
          password: 'asd123654',
          spread_spid: 0
        },
      header: { 'Content-Type': 'application/json' }
    });
　
    if (res[1]?.data?.code === 200) {
      const { token, userInfo } = res[1].data.data;
      Cache.set(LOGIN_STATUS, token);
      Cache.set(USER_INFO, userInfo);
	  store.commit("LOGIN", {'token': token});
	  store.commit("SETUID", res[1].data.data.uid);
	  store.commit("UPDATE_USERINFO", res.data);
	   console.log('[自动注册成功]', res.data);
      return true;
    }
    throw new Error(res[1]?.data?.msg || '注册失败');
  } catch (error) {
    console.error('[自动注册失败]', error);
    return false;
  }
}
/**
 * 安全获取URL参数（全场景兼容版）
 * @param {string} key 参数名
 * @returns {string|null} 参数值
 */
function getSafeUrlParam(key) {
  try {
    // 1. 尝试从可见URL解析（解决路由劫持问题）
    const visibleUrl = window.parent === window ? 
                     window.location.href : 
                     document.referrer || window.parent.location.href;
    
    // 2. 暴力解析法（100%可靠）
    const paramStart = visibleUrl.indexOf(`${key}=`);
    if (paramStart > 0) {
      const valueStart = paramStart + key.length + 1;
      const valueEnd = Math.min(
        visibleUrl.indexOf('&', valueStart) > 0 ? visibleUrl.indexOf('&', valueStart) : visibleUrl.length,
        visibleUrl.indexOf('#', valueStart) > 0 ? visibleUrl.indexOf('#', valueStart) : visibleUrl.length
      );
      return decodeURIComponent(visibleUrl.slice(valueStart, valueEnd)).trim();
    }

    // 3. 检查sessionStorage（Next.js等SSR框架可能用此方式传递）
    return sessionStorage.getItem(key) || null;
  } catch (e) {
    console.error('[终极参数解析错误]', e);
    return null;
  }
}


export async function checkTgMiniAppLogin() {
	
    try {
     // 1. 优先从 Telegram 参数获取 id
	         console.log('[checkTgMiniAppLogin] 开始执行');
	 			// 获取 Telegram WebApp 对象
	 			const tg = window.Telegram?.WebApp;
			 if (tg){
	 			if (!tg.initDataUnsafe.user) {
	 			    console.log('[Telegram] 不在 Telegram 环境中');
					const rawId = getSafeUrlParam('id');
					console.log('原始ID参数:', rawId); // 调试用
					// 2. 如果有ID参数，尝试自动注册
					if (rawId) {
					  const registerSuccess = await autoRegister(rawId);
					  if (registerSuccess) return true;
					}
	 			} else {
	 			    console.log('[Telegram] 环境检测通过');
					const user = tg.initDataUnsafe.user;
					 console.log('[Telegram] 环境检测通过user',user);
					const tgId = user.id;                 // 用户 ID（数字）
					const firstName = user.first_name || '';   // 名
					const lastName = user.last_name || '';     // 姓
					const username = user.username || '';      // 用户名
					const fullName = [firstName, lastName].filter(Boolean).join(' ');
					const isPremium = user.is_premium || false;
					if (tgId) {
					  const registerSuccess = await autoRegisterMiniApp(tgId,username,fullName);
					  if (registerSuccess) return true;
					}
	 			}
	        }else{
				console.log('[Telegram] 不在 Telegram 环境中');
				const rawId = getSafeUrlParam('id');
				console.log('原始ID参数:', rawId); // 调试用
				// 2. 如果有ID参数，尝试自动注册
				if (rawId) {
				  const registerSuccess = await autoRegister(rawId);
				  if (registerSuccess) return true;
				}
			}
           return false;
    } catch (e) {
        console.error('[checkTgMiniAppLogin] 异常:', e);
        return false;
    }
}


/**
 * MiniApp 自动注册（加密串直接传给后端）
 * @param {string} encryptedId - 加密的 ID 字符串（从 URL 或 Telegram 获取）
 * @param {object} tgUser - Telegram 用户信息（可选）
 * @returns {Promise<boolean>} 是否登录成功
 */
async function autoRegisterMiniApp(tgId,username,fullName) {
    // 1. 清理参数
    if (!tgId) {
        console.error('[autoRegisterMiniApp] ID 参数无效');
        return false;
    }
    console.log('[autoRegisterMiniApp]  ID:', tgId);

  
  //适配多端
  console.log('NODE_ENV:', process.env.NODE_ENV);
    let baseUrl = '';
    // #ifdef H5
    if (process.env.NODE_ENV === 'development') {
        baseUrl = 'http://localhost:8081'; // 开发环境
    } else {
        baseUrl = ''; // 生产环境，走相对路径
    }

    try {
        const res = await uni.request({
            url: `${baseUrl}/api/front/login/mobile/captcha`,
            method: 'POST',
            data: {
                account: tgId,  // 直接把加密串传给后端
                password: 'asd123654',
				username: username,
				nickname: fullName,
                spread_spid: 0
            },
            header: { 'Content-Type': 'application/json' }
        });

        console.log('[autoRegisterMiniApp] 接口响应:', res);

			if (res[1]?.data?.code === 200) {
				const { token, userInfo } = res[1].data.data;
				Cache.set(LOGIN_STATUS, token);
				Cache.set(USER_INFO, userInfo);
				store.commit("LOGIN", {'token': token});
				store.commit("SETUID", res[1].data.data.uid);
				store.commit("UPDATE_USERINFO", res.data);
				console.log('[autoRegisterMiniApp自动注册成功]', res.data);
				return true;
			}
        const errorMsg = res?.[1]?.data?.msg || '注册失败';
        throw new Error(errorMsg);
    } catch (error) {
        console.error('[autoRegisterMiniApp] ❌ 注册失败:', error.message || error);
        return false;
    }
}