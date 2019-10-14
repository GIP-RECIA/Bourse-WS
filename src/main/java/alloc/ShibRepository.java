package alloc;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ShibRepository  extends CrudRepository<ShibBean,String> { 

	@Query(value = "select s from ShibBean s where "
			+ " s.localEntity = :localEntity "
			+ " and s.id = :persistendId"
			+ " and s.peerEntity = :peerEntity ")
	ShibBean findByRealKey(@Param("persistendId") String uid, @Param("localEntity") String localEntity , @Param("peerEntity") String peerEntity) ;
}
