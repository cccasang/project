package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

//自定义权限管理实现类
//根据用户名到数据库中的seller表中获取用户详细信息
//返回springSecurity规定的User对象,给SpringSecurity用户名和密码 ,还有如果登录成功应该具有的访问权限集合
public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //创建权限集合
        List<GrantedAuthority> authList = new ArrayList<>();
        //给权限集合加入对应的访问权限
        authList.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        Seller seller = sellerService.findOne(username);

        if (seller != null){
            if ("1".equals(seller.getStatus())){
                return new User(username,seller.getPassword(),authList);
            }
        }

        return null;
    }


}
