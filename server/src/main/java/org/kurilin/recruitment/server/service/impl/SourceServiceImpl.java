package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.SourceDAO;
import org.kurilin.recruitment.server.service.SourceService;
import org.kurilin.recruitment.shared.entity.Source;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.SourceResponseDTO;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class SourceServiceImpl implements SourceService {
    private static final Logger logger = LoggerFactory.getLogger(SourceServiceImpl.class);
    private final SourceDAO sourceDAO;
    private final Gson gson = GsonFactory.getGson();

    public SourceServiceImpl(SourceDAO sourceDAO) {
        this.sourceDAO = sourceDAO;
    }

    @Override
    public Response getAllSources() throws RecruitmentBusinessException {
        logger.info("Get all sources request");

        List<Source> sources = sourceDAO.findAll(Source.class);
        if (sources.isEmpty()) {
            throw new RecruitmentBusinessException("No sources found");
        }
        List<SourceResponseDTO> responseDTOList = sources.stream()
                .map(source -> SourceResponseDTO.builder()
                        .id(source.getId())
                        .name(source.getName())
                        .build())
                .toList();
        logger.info("Sources found: {}", responseDTOList.size());

        return new Response(true, "Sources retrieved successfully", gson.toJson(responseDTOList));
    }
}
