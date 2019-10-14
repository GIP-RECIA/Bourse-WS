package alloc;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ShibRepository  extends CrudRepository<Shibpid,String> { 

	@Query(value = "select s from Shibpid s where "
			+ " s.localEntity = :localEntity "
			+ " and s.persistentId = :persistendId"
			+ " and s.peerEntity = :peerEntity ")
	Shibpid findByRealKey(@Param("persistendId") String shibpid, @Param("localEntity") String localEntity , @Param("peerEntity") String peerEntity) ;
	
	
}
