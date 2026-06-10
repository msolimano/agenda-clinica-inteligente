package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

abstract class BIRepositorySupport {

    protected MapSqlParameterSource params(BIFilter filter) {
        return new MapSqlParameterSource()
            .addValue("from", Timestamp.from(filter.from()), Types.TIMESTAMP)
            .addValue("to", Timestamp.from(filter.to()), Types.TIMESTAMP)
            .addValue("professionalId", filter.professionalId(), Types.OTHER)
            .addValue("specialtyId", filter.specialtyId(), Types.OTHER);
    }

    protected String professionalAndSpecialtyFilter(String alias) {
        return """
              and (cast(:professionalId as uuid) is null or %s.professional_id = cast(:professionalId as uuid))
              and (cast(:specialtyId as uuid) is null or exists (
                  select 1
                  from professional_specialties ps
                  where ps.professional_id = %s.professional_id
                    and ps.specialty_id = cast(:specialtyId as uuid)
                    and ps.status = 'active'
              ))
            """.formatted(alias, alias);
    }

    protected long longValue(ResultSet rs, String column) throws SQLException {
        return rs.getLong(column);
    }
}
