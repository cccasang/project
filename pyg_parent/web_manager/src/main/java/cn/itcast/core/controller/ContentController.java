package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告管理
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;


    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Content content){
        return contentService.search(page, rows, content);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody Content content){
        try {
            contentService.add(content);
            return new Result(true,"广告添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"广告添加失败");

        }
    }

    @RequestMapping("/findOne")
    public Content findOne(Long id){
        return contentService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Content content){
        try {
            contentService.update(content);
            return new Result(true,"广告更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"广告更新失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentService.delete(ids);
            return new Result(true,"广告删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"广告删除失败");
        }
    }
}
