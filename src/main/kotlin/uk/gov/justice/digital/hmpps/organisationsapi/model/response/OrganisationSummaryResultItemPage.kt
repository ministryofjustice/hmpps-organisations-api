package uk.gov.justice.digital.hmpps.organisationsapi.model.response

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

// This is not directly used but is required to make Swagger produce a spec with page and content fields typed correctly
class OrganisationSummaryResultItemPage(content: List<OrganisationSummary>, pageable: Pageable, total: Long) : PageImpl<OrganisationSummary>(content, pageable, total)
