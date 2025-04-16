package bonun.bustime.repository;


import bonun.bustime.entity.NodeIdEntity;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface StopListByRouteIdRepository extends JpaRepository<NodeIdEntity, String> {


    boolean existsByNodeId(String nodeId);
    List<NodeIdEntity> findAll();

}