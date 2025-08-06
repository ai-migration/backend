package backend.infra;

import backend.config.kafka.KafkaProcessor;
import backend.domain.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;

import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    PostRepository postRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PostRegisterRequested'"
    )
    public void wheneverPostRegisterRequested_RegisterPostPolicy(
        @Payload PostRegisterRequested postRegisterRequested
    ) {
        PostRegisterRequested event = postRegisterRequested;
        System.out.println(
            "\n\n##### listener RegisterPostPolicy : " +
            postRegisterRequested +
            "\n\n"
        );

        // Sample Logic //
        Post post = new Post();
        post.setPostId(postRegisterRequested.getPostId());
        post.setTitle(postRegisterRequested.getTitle());
        post.setContent(postRegisterRequested.getContent());
        post.setType(postRegisterRequested.getType());
        post.setCreatedAt(new Date()); 
        postRepository.save(post);

        PostRegistered postRegistered = new PostRegistered(post);
        postRegistered.publishAfterCommit();
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PostDeleteRequested'"
    )
    public void wheneverPostDeleteRequested_DeletePostPolicy(
        @Payload PostDeleteRequested postDeleteRequested
    ) {
        PostDeleteRequested event = postDeleteRequested;
        System.out.println(
            "\n\n##### listener DeletePostPolicy : " +
            postDeleteRequested +
            "\n\n"
        );

        // Sample Logic //
        Post post = postRepository.findByPostId(event.getPostId())
            .orElseThrow(() -> new RuntimeException("Post Not Found"));
        postRepository.delete(post);

        PostDeleted postDeleted = new PostDeleted(post);
        postDeleted.publishAfterCommit();
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PostUpdateRequested'"
    )
    public void wheneverPostUpdateRequested_UpdatePostPolicy(
        @Payload PostUpdateRequested postUpdateRequested
    ) {
        PostUpdateRequested event = postUpdateRequested;
        System.out.println(
            "\n\n##### listener UpdatePostPolicy : " +
            postUpdateRequested +
            "\n\n"
        );
        Post post = postRepository.findByPostId(event.getPostId()).orElseThrow(() -> new RuntimeException("Post Not Found"));

        // Sample Logic //
        post.setTitle(event.getTitle());
        post.setContent(event.getContent());
        post.setType(event.getType());
        post.setUpdatedAt(new Date());

        postRepository.save(post);

        PostUpdated postUpdated = new PostUpdated(post);
        postUpdated.publishAfterCommit();
    }
}
//>>> Clean Arch / Inbound Adaptor
