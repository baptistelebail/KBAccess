package org.opens.kbaccess.entity.dao.reference;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.opens.kbaccess.entity.reference.Reference;
import org.opens.kbaccess.entity.reference.ReferenceImpl;
import org.opens.tanaguru.sdk.entity.dao.jpa.AbstractJPADAO;

public class ReferenceDAOImpl extends AbstractJPADAO<Reference, Long> implements
        ReferenceDAO {

    public ReferenceDAOImpl() {
        super();
    }

    @Override
    protected Class<ReferenceImpl> getEntityClass() {
        return ReferenceImpl.class;
    }

    @Override
    public Reference findByCode(String code) {
        try {
            Query query = entityManager.createQuery("SELECT n FROM "
                    + getEntityClass().getName() + " n" + " WHERE n.code = :code");
            query.setParameter("code", code);
            return (Reference) query.getSingleResult();
        } catch (NoResultException e) {
            // In case of query with no result, return null
            return null;
        }
    }
}