
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 教练留言
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/jiaolianLiuyan")
public class JiaolianLiuyanController {
    private static final Logger logger = LoggerFactory.getLogger(JiaolianLiuyanController.class);

    private static final String TABLE_NAME = "jiaolianLiuyan";

    @Autowired
    private JiaolianLiuyanService jiaolianLiuyanService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private JiaolianService jiaolianService;//教练
    @Autowired
    private JiaolianCollectionService jiaolianCollectionService;//教练收藏
    @Autowired
    private JiaolianYuyueService jiaolianYuyueService;//教练预约
    @Autowired
    private JiaoxiaoService jiaoxiaoService;//驾校信息
    @Autowired
    private KaoshixService kaoshixService;//考试通知
    @Autowired
    private KaoshixYuyueService kaoshixYuyueService;//考试预约
    @Autowired
    private NewsService newsService;//公告通知
    @Autowired
    private YonghuService yonghuService;//学员
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("学员".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("教练".equals(role))
            params.put("jiaolianId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = jiaolianLiuyanService.queryPage(params);

        //字典表数据转换
        List<JiaolianLiuyanView> list =(List<JiaolianLiuyanView>)page.getList();
        for(JiaolianLiuyanView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiaolianLiuyanEntity jiaolianLiuyan = jiaolianLiuyanService.selectById(id);
        if(jiaolianLiuyan !=null){
            //entity转view
            JiaolianLiuyanView view = new JiaolianLiuyanView();
            BeanUtils.copyProperties( jiaolianLiuyan , view );//把实体数据重构到view中
            //级联表 教练
            //级联表
            JiaolianEntity jiaolian = jiaolianService.selectById(jiaolianLiuyan.getJiaolianId());
            if(jiaolian != null){
            BeanUtils.copyProperties( jiaolian , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"
, "jiaolianId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setJiaolianId(jiaolian.getId());
            }
            //级联表 学员
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(jiaolianLiuyan.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"
, "jiaolianId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JiaolianLiuyanEntity jiaolianLiuyan, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,jiaolianLiuyan:{}",this.getClass().getName(),jiaolianLiuyan.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("学员".equals(role))
            jiaolianLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        else if("教练".equals(role))
            jiaolianLiuyan.setJiaolianId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        jiaolianLiuyan.setCreateTime(new Date());
        jiaolianLiuyan.setInsertTime(new Date());
        jiaolianLiuyanService.insert(jiaolianLiuyan);

        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JiaolianLiuyanEntity jiaolianLiuyan, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,jiaolianLiuyan:{}",this.getClass().getName(),jiaolianLiuyan.toString());
        JiaolianLiuyanEntity oldJiaolianLiuyanEntity = jiaolianLiuyanService.selectById(jiaolianLiuyan.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("学员".equals(role))
//            jiaolianLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
//        else if("教练".equals(role))
//            jiaolianLiuyan.setJiaolianId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        jiaolianLiuyan.setUpdateTime(new Date());

            jiaolianLiuyanService.updateById(jiaolianLiuyan);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<JiaolianLiuyanEntity> oldJiaolianLiuyanList =jiaolianLiuyanService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        jiaolianLiuyanService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<JiaolianLiuyanEntity> jiaolianLiuyanList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            JiaolianLiuyanEntity jiaolianLiuyanEntity = new JiaolianLiuyanEntity();
//                            jiaolianLiuyanEntity.setJiaolianId(Integer.valueOf(data.get(0)));   //教练 要改的
//                            jiaolianLiuyanEntity.setYonghuId(Integer.valueOf(data.get(0)));   //学员 要改的
//                            jiaolianLiuyanEntity.setJiaolianLiuyanText(data.get(0));                    //留言内容 要改的
//                            jiaolianLiuyanEntity.setInsertTime(date);//时间
//                            jiaolianLiuyanEntity.setReplyText(data.get(0));                    //回复内容 要改的
//                            jiaolianLiuyanEntity.setUpdateTime(sdf.parse(data.get(0)));          //回复时间 要改的
//                            jiaolianLiuyanEntity.setCreateTime(date);//时间
                            jiaolianLiuyanList.add(jiaolianLiuyanEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        jiaolianLiuyanService.insertBatch(jiaolianLiuyanList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = jiaolianLiuyanService.queryPage(params);

        //字典表数据转换
        List<JiaolianLiuyanView> list =(List<JiaolianLiuyanView>)page.getList();
        for(JiaolianLiuyanView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiaolianLiuyanEntity jiaolianLiuyan = jiaolianLiuyanService.selectById(id);
            if(jiaolianLiuyan !=null){


                //entity转view
                JiaolianLiuyanView view = new JiaolianLiuyanView();
                BeanUtils.copyProperties( jiaolianLiuyan , view );//把实体数据重构到view中

                //级联表
                    JiaolianEntity jiaolian = jiaolianService.selectById(jiaolianLiuyan.getJiaolianId());
                if(jiaolian != null){
                    BeanUtils.copyProperties( jiaolian , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setJiaolianId(jiaolian.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(jiaolianLiuyan.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody JiaolianLiuyanEntity jiaolianLiuyan, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,jiaolianLiuyan:{}",this.getClass().getName(),jiaolianLiuyan.toString());
        jiaolianLiuyan.setCreateTime(new Date());
        jiaolianLiuyan.setInsertTime(new Date());
        jiaolianLiuyanService.insert(jiaolianLiuyan);

            return R.ok();
        }

}

