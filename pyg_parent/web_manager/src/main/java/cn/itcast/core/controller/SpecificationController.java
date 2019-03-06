package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.entity.Spec;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//品牌管理
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    //查询品牌所有数据
    @RequestMapping("/findAll")
    public List<Specification> findAll(){
        return specificationService.findAll();
    }

    /**
     * 分页查询
     * @param page  当前页
     * @param rows  每页显示多少条数据
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer page, Integer rows){
        return specificationService.findPage(null,page,rows);
    }


    /**
     * 添加品牌数据
     * @param spec
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Spec spec){
        try {
            specificationService.add(spec);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**
     * 回显修改数据
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Spec findOne(Long id){
        Spec one = specificationService.findOne(id);
        return one;

    }

    /**
     * 修改数据
     * @param spec
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody  Spec spec){
        try {
            specificationService.update(spec);
            return new Result(true,"修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败!");
        }
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"删除失败!");

        }
    }

    /**
     * 高级分页查询
     * @param specification 品牌查询对象
     * @param page  当前页
     * @param rows  每页显示的条数
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Specification specification,Integer page, Integer rows){
       return specificationService.findPage(specification,page,rows);
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();
    }
}
