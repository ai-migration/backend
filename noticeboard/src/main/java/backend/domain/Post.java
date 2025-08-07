package backend.domain;

import backend.NoticeboardApplication;

import java.util.Date;
import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "Post_table")
@Data
//<<< DDD / Aggregate Root
public class Post {

    @Id
    private Long postId;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @Column(updatable = false)
    private Date createdAt;

    private Date updatedAt;

    public static PostRepository repository() {
        PostRepository postRepository = NoticeboardApplication.applicationContext.getBean(
            PostRepository.class
        );
        return postRepository;
    }

    //<<< Clean Arch / Port Method
    public static void registerPostPolicy(
        PostRegisterRequested postRegisterRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item */
        Post post = new Post();
        post.setPostId(postRegisterRequested.getPostId());
        post.setTitle(postRegisterRequested.getTitle());
        post.setContent(postRegisterRequested.getContent());
        post.setType(postRegisterRequested.getType());
        post.setCreatedAt(new Date());

        repository().save(post);

        PostRegistered postRegistered = new PostRegistered(post);
        postRegistered.setPostId(post.getPostId());
        postRegistered.setTitle(post.getTitle());
        postRegistered.setContent(post.getContent());
        postRegistered.setType(post.getType());
        postRegistered.setCreatedAt(post.getCreatedAt());

        postRegistered.publishAfterCommit();
        

        /** Example 2:  finding and process
        

        repository().findById(postRegisterRequested.get???()).ifPresent(post->{
            
            post // do something
            repository().save(post);

            PostRegistered postRegistered = new PostRegistered(post);
            postRegistered.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void deletePostPolicy(
        PostDeleteRequested postDeleteRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Post post = new Post();
        repository().save(post);

        PostDeleted postDeleted = new PostDeleted(post);
        postDeleted.publishAfterCommit();
        */

        /** Example 2:  finding and process*/
        

        repository().findById(postDeleteRequested.getPostId()).ifPresent(post->{
            
            // do something
            repository().delete(post);

            PostDeleted postDeleted = new PostDeleted(post);
            postDeleted.setPostId(post.getPostId());
            postDeleted.publishAfterCommit();

         });

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void updatePostPolicy(
        PostUpdateRequested postUpdateRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Post post = new Post();
        repository().save(post);

        PostUpdated postUpdated = new PostUpdated(post);
        postUpdated.publishAfterCommit();
        */

        /** Example 2:  finding and process*/
        

        repository().findById(postUpdateRequested.getPostId()).ifPresent(post->{
            
            post.setTitle(postUpdateRequested.getTitle()); // do something
            post.setContent(postUpdateRequested.getContent());
            post.setUpdatedAt(new Date());
            repository().save(post);

            PostUpdated postUpdated = new PostUpdated(post);
            postUpdated.setPostId(post.getPostId());
            postUpdated.setTitle(post.getTitle());
            postUpdated.setContent(post.getContent());
            postUpdated.setUpdatedAt(post.getUpdatedAt());

            postUpdated.publishAfterCommit();

         });


    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
