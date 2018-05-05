package cn.gzsxt.lucene;


import cn.gzsxt.entity.Book;
import cn.gzsxt.util.DbUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Demo1 {


    /**
     * 数据采集,放入索引库中
     * <p>
     * 创建索引库
     */
    @Test
    public void test1() {
        List<Document> list = new ArrayList<Document>();
        Connection connection = DbUtil.getConnection();
        try {
            //1.分词器 StandarAndlyzer 默认分词器
            Analyzer analyzer = new StandardAnalyzer();

            //2.初始化索引库位置
            FSDirectory directory = FSDirectory.open(new File("F:/IndexWriter"));
            //3.初始化索引流
            //Vsersion.LATEST 当前依赖的版本
            IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
            //创建索引流对象
            //1:写入那个库中
            //2:初始化的索引流里面带有的分词对象
            IndexWriter writer = new IndexWriter(directory, config);


            //4.数据库读取,放入Document对象内.
            PreparedStatement ps = connection.prepareStatement("select * from book");
            ResultSet resultSet = ps.executeQuery();
            Document doc = null;
            while (resultSet.next()) {
                doc = new Document();
                doc.add(new TextField("id", resultSet.getString("id"), Field.Store.YES));
                doc.add(new TextField("name", resultSet.getString("name"), Field.Store.YES));
                doc.add(new FloatField("price", Float.parseFloat(resultSet.getString("price")), Field.Store.YES));
                doc.add(new TextField("pic", resultSet.getString("pic"), Field.Store.YES));
                doc.add(new TextField("description", resultSet.getString("description"), Field.Store.NO));
                list.add(doc);
            }
            //5.将文档写入索引库中
            writer.addDocuments(list);

            writer.close();
            ;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtil.close();
        }

    }


    /**
     * Field 常用类型
     * 分词:分词处理,即将Field域中的value进行分词,分词的目的是为了索引
     * <p>
     * 索引:将Field分词后的词或整个Field值进行索引,索引目的是为了搜索
     * <p>
     * 储存:将Field值存储在文档中,存储在文档中的Field可以从Document中获取
     * <p>
     * <p>
     * 分词  索引   存储
     * StringField    N    Y     Y/N
     * <p>
     * LongField      Y    Y     Y/N
     * <p>
     * FloatField     Y    Y     Y/N
     * <p>
     * StoredField    N    N     Y
     * <p>
     * TextField      Y    Y      N
     */
    @Test
    public void test2() {

        try {
            //默认的分词器
            Analyzer analyzer = new StandardAnalyzer();

            //创建索引库
            FSDirectory directory = FSDirectory.open(new File("F:/IndexWriter"));

            //初始化索引流
            IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);

            //创建索引流
            IndexWriter writer = new IndexWriter(directory, config);

            Document doc = new Document();
            doc.add(new StoredField("id", 1));
            doc.add(new TextField("name", "007", Field.Store.YES));
            doc.add(new FloatField("price", 100f, Field.Store.YES));
            doc.add(new StoredField("pic", "007.jpg"));
            doc.add(new TextField("description", "这是周星驰演的007", Field.Store.NO));

            writer.addDocument(doc);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 删除索引库
     * <p>
     * deleteAll 删除索引库中的全部数据(慎重使用)
     * <p>
     * deleteDocuments,参数可以放入Term 或者Query对象
     * <p>
     * Term就是直接把Field中的value 不以分词的方式直接查询出来之后,直接删除
     * TermQuery 和Term都是一样不分词的
     * <p>
     * Query子类查询分为2中,需要分词删除的,请看下面的分词查询
     */
    @Test
    public void deleteIndexDoc() {

        try {
            Analyzer analyzer = new StandardAnalyzer();

            FSDirectory directory = FSDirectory.open(new File("F:/IndexWriter"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);

            IndexWriter writer = new IndexWriter(directory, config);
            writer.deleteAll();

            //writer.deleteDocuments(new Term("name","007"));
           // TermQuery query = new TermQuery(new Term("name", "007"));
            //writer.deleteDocuments(query);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 修改索引
     *
     * updateDocument
     *
     * 先进行查询索引去匹配,匹配得到的,全部替换成新的索引,(如果是就替换一个域的索引,请把其他域的一起带如,不然整个文档都会不在了就保存新文档)
     *
     * 如果没有匹配到对应的索引,那么这份文档会重新再索引库中建立索引.
     *
     */
    @Test
    public void updateIndex() {
        try {
            Analyzer analyzer = new StandardAnalyzer();
            FSDirectory directory = FSDirectory.open(new File("F:/IndexWriter"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
            IndexWriter writer = new IndexWriter(directory, config);


            Document doc = new Document();
            doc.add(new TextField("name","008",Field.Store.YES));
            writer.updateDocument(new Term("name","007"),doc);
            writer.close();;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询公共方法
     * @param query
     */
    public void doSearch(Query query){
        try {
            //指定索引库
            FSDirectory open = FSDirectory.open(new File("F:/IndexWriter"));
            //DirectoryReader  读取索引库
            IndexReader reader = DirectoryReader.open(open);
            //建立索引查询
            IndexSearcher searcher = new IndexSearcher(reader);
            //参数1:查询的对象
            //参数2:查询最大条数
            TopDocs docs = searcher.search(query, 10);

            System.out.println("一共查询出:"+docs.totalHits+"条数据");
            ScoreDoc[] docs1 = docs.scoreDocs;

            for (ScoreDoc dcoreDoc: docs1
                 ) {
                //获取出DocId
                int docId = dcoreDoc.doc;
                //使用IndexSearcher,查询docId 获取对应的索引文档
                Document doc = searcher.doc(docId);
                // f) 输出文档内容
                System.out.println(doc.get("name"));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * TermQuery 查询,TermQuery不使用分析器进行查询,搜索关键词作为整体来匹配Field域中的词进行查询,比如订单号.分类ID号等.
     */
    @Test
    public void testTermQuery(){

        Term term = new Term("name", "java");

        TermQuery query = new TermQuery(term);

        this.doSearch(query);

    }

    /**
     * 指定数字范围查询(创建Field类型时,注意之对应)
     */
    @Test
    public void testNumericRangeQuery(){
        /*
        // 创建查询
		// 第一个参数：域名
		// 第二个参数：最小值
		// 第三个参数：最大值
		// 第四个参数：是否包含最小值
		// 第五个参数：是否包含最大值
         */
        Query query = NumericRangeQuery.newFloatRange("price",55f,71.5f,false,true);

        this.doSearch(query);

    }

    /**
     * 布尔查询，实现组合条件查询。
     *
     *  MUST:查询条件必须满足，相当于AND
     *  MUST_NOT:查询条件不能满足，相当于NOT非
     *  SHOULD:查询条件可选，相当于OR
     *
     *
     *  1、MUST和MUST表示“与”的关系，即“并集”。
     *  2、MUST和MUST_NOT前者包含后者不包含。
     *  3、MUST_NOT和MUST_NOT没意义
     *  4、SHOULD与MUST表示MUST，SHOULD失去意义；
     *  5、SHOUlD与MUST_NOT相当于MUST与MUST_NOT。
     *   6、SHOULD与SHOULD表示“或”的概念。
     *
     */
    @Test
    public void testBooleanQuery(){
        BooleanQuery query = new BooleanQuery();

        Query query1 = new TermQuery(new Term("name","java"));

        Query query2 = NumericRangeQuery.newFloatRange("price",55f,71.5f,false,true);

        query.add(query1,BooleanClause.Occur.MUST);
        query.add(query2,BooleanClause.Occur.SHOULD);

        this.doSearch(query);
    }

    /**
     *
     * QueryParser
     * 通过第三方分词器来进行解析
     *  IkAnalyzer
     *
     *  QueryParser 不支持数字查询,如果要用到数字进行全文索引,请用文NumerRangeQuery
     *
     */
    @Test
    public  void testQueryParser(){
        try {
            //1:选择查询的域
            //2:分词器
            QueryParser queryParser = new QueryParser("name", new StandardAnalyzer());
            //指定查询的语法
            //这里" " 代表or, 大写AND 代表这两个词都要查询出来
            Query query = queryParser.parse("java AND lucene");
            this.doSearch(query);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * MultiFieldQueryParser
     *
     * 多域查询
     *
     *
     *
     */
    @Test
    public void testMultiFieldQueryParser(){


        try {

            String[] field = {"name","description"};


            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(field, new StandardAnalyzer());
            //指定查询的值,会在多域中同时一起查询
            Query query = queryParser.parse("java lucene");

            this.doSearch(query);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }









}
