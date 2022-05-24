package com.example.demo.src.post;


import com.example.demo.src.post.model.GetPostImgRes;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PostImgsUrlReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetPostsRes> selectPosts(int userIdx) {
        String selectPostsQuery = "SELECT p.postIdx as postIdx,\n" +
                "u.userIdx as userIdx,\n" +
                "u.nickName as nickName,\n" +
                "u.profileImgUrl as profileImgUrl,\n" +
                "p.content as content,\n" +
                "IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                "IF(commentCount is null, 0, commentCount) as commentCount,\n" +
                "case when timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                "then concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                "when timestampdiff(minute, p.updatedAt, current_timestamp) < 60\n" +
                "then concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                "when timestampdiff(hour, p.updatedAt, current_timestamp) < 24\n" +
                "then concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                "when timestampdiff(day, p.updatedAt, current_timestamp) < 365\n" +
                "then concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                "else timestampdiff(year, p.updatedAt, current_timestamp)\n" +
                "end as updatedAt,\n" +
                "IF(pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                "FROM Post as p\n" +
                "join User as u on u.userIdx = p.userIdx\n" +
                "left join (select postIdx, userIdx, count(postLikeIdx) as postLikeCount from PostLike WHERE status = 'ACTIVE') pi on pi.postIdx = p.postIdx\n" +
                "left join (select postIdx, count(commentIdx) as commentCount from Comment WHERE status = 'ACTIVE') c on c.postIdx = p.postIdx\n" +
                "left join Follow as f on f.followeeIdx = p.userIdx and f.status='ACTIVE'\n" +
                "left join PostLike as pl on pl.userIdx = f.followerIdx and pl.postIdx = p.postIdx\n" +
                "WHERE f.followerIdx = ? and p.status = 'ACTIVE'\n" +
                "group by p.postIdx;";

        int selectPostsParam = userIdx;
        return this.jdbcTemplate.query(selectPostsQuery,
                (rs, rowNum) -> new GetPostsRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickname"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNot"),
                        getPostImgRes = this.jdbcTemplate.query("SELECT pi.postImgUrlIdx, pi.imgUrl\n" +
                                        "                                 FROM PostImgUrl as pi\n" +
                                        "                                join Post as p on p.postIdx = pi.postIdx\n" +
                                        "                                WHERE pi.status = 'ACTIVE' and p.postIdx = ?;",
                                (rk, rownum) -> new GetPostImgRes(
                                        rk.getInt("postImgUrlIdx"),
                                        rk.getString("imgUrl")
                                ),rs.getInt("postIdx")
                                )
                ),selectPostsParam);
    }
    //User validation
    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);
    }
    //Post validation
    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParams = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery,
                int.class,
                checkPostExistParams);
    }

    //Post 생성
    //insert문 사용할 때는 update 필요 (return X)
    public int insertPosts(int userIdx, String content){
        String insertPostsQuery = "INSERT INTO Post(userIdx, content) VALUES (?,?)";
        //쿼리문 통해 받아온 값 넣어주기
        Object []insertPostsParams= new Object[] {userIdx, content};
        this.jdbcTemplate.update(insertPostsQuery, insertPostsParams);

        //해당 쿼리문은 자동으로 마지막에 들어간 id값
       String lastInsertIdxQuery = "select last_insert_id()";
       return this.jdbcTemplate.queryForObject(lastInsertIdxQuery,int.class);
    }

    //postImg 생성
  public int insertPostsImgs(int postIdx, PostImgsUrlReq postImgsUrlReq){
        String insertPostsImgsQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?,?)";
        //쿼리문 통해 받아온 값 넣어주기
        Object [] insertPostsImgsParams= new Object[] {postIdx, postImgsUrlReq.getImgUrl()};
        this.jdbcTemplate.update(insertPostsImgsQuery, insertPostsImgsParams);

        String lastInsertIdxQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery,int.class);
    }

    //post content 수정
    public int updatePost(int postIdx, String content){
        String updatePostQuery = "UPDATE Post SET content=? WHERE postIdx=?";
        //쿼리문 통해 받아온 값 넣어주기
        Object [] updatePostParams= new Object[] {content,postIdx};
        return this.jdbcTemplate.update(updatePostQuery, updatePostParams);
    }

    //post 삭제
    public int deletePost(int postIdx){
        String updatePostQuery = "UPDATE Post SET status='INACTIVE' WHERE postIdx=?";
        //쿼리문 통해 받아온 값 넣어주기
        Object [] deletePostParams= new Object[] {postIdx};
        return this.jdbcTemplate.update(updatePostQuery, deletePostParams);
    }






}
