# Legacy NOMIS field extraction research

This document will capture fields visible in legacy NOMIS screenshots and compare them against the current organisation data model in this codebase.

## Working notes
- Goal: extract every field visible in legacy NOMIS screenshots.
- Follow-up: compare captured fields to the current application model and identify gaps.
- Add each screenshot as a separate section below with the fields that are explicitly visible.

## Screenshot 1

Source: legacy NOMIS screen titled "Maintain Agencies/Corporations" (agency/corporation maintenance screen)

### Extracted field names visible on-screen

#### Corporate Information
- Corporate Name*
- Caseload
- Programme No.
- VAT
- Corporate ID
- Comment
- Corporate Types
- Active
- Expiry Date

Observed sample values:
- Corporate Name: "W"
- Caseload: "EASTWOOD PARK (H)"
- Corporate ID: "4594"
- Active: checked
- Expiry Date: blank

#### Address Information
- House Information
- Street Information
- Area Information
- Postal Code
- Country
- Type
- Special Needs
- Service 
- Primary
- Mail 
- Active

Observed sample rows:
- UVS uSVS / nWslkyHVUtnWslky / PlbVfdPPlbVfdP / M A3 6TO / England / BUS / unchecked / unchecked / unchecked / checked
- xRPXGxCMxRPXGxC / Manchester Great / W21 8PW / England / BUS / unchecked / unchecked / checked / checked
- hHSajhHSa / Romford Essex / EG9 8FE / England / unchecked / unchecked / unchecked / unchecked
- yFNwxyFNw / Romford Essex / OM9 6VM / England / unchecked / unchecked / unchecked / unchecked
- PVZMXKFPVZMXKF / wmMawmMa / London / S3C 0JD / England / unchecked / unchecked / unchecked / unchecked
- mEAcjdsyRmEAcjds / IEMDoKylEMDoK / KA1 6PE / England / BUS / unchecked / unchecked / unchecked / unchecked

#### Address Specific Numbers
- Phone Type
- Phone No.*
- Extension

Observed sample rows:
- Business / 3484848484 / blank
- Business / 976 555 58305 / blank

#### Global/Email/Web
- Type*
- Addresses

Observed sample values:
- Type: blank or not populated in the visible area
- Address rows appear in the list but specific values are not readable in the image

#### Controls/actions in view
- Contact/Business Hours
- Address Add/Query

### Notes on interpretation
- This screen appears to represent an organisation or agency record with multiple related contact/address records.
- The visible legacy NOMIS fields map to the kinds of data we would expect on a modern organisation record: name, identifiers, addresses, phone numbers, email/web details, and flags for active/main/service status.
- Some field labels are visually repeated or grouped as a form section, but the above list captures all readable labels and sample information from the screenshot.

## Screenshot 2

Source: legacy NOMIS lookup window for Phone Types, domain = "PHONE_USAGE"

### Extracted phone type values
- Home (code: HOME)
- Business (code: BUS)
- Fax (code: FAX)
- Alternate Business (code: ALTB)
- Alternate Home (code: ALTH)
- Mobile (code: MOB)
- Agency Visit Line (code: VISIT)

### Notes
- This confirms the legacy phone type classification used in the organisation screen is a controlled list with a domain name of `PHONE_USAGE`.
- The visible screen provides both the human-readable description and the stored code value.

## Screenshot 3

Source: legacy NOMIS lookup window for Global Email/Web Type, domain = "IADDR_CLASS"

### Extracted internet address type values
- Email Address (code: EMAIL)
- Web Address (code: WEB)

### Notes
- This confirms the associated contact-type classification for the email/web section is a controlled list with a domain name of `IADDR_CLASS`.
- The list is a short, simple set with two supported values: email and web.

## Screenshot 4

Source: legacy NOMIS modal opened from the "Contact/Business Hours" button.

### Extracted fields visible on-screen
- Contact Person
- Business Hours

Buttons
- Save
- Exist

### Observed sample values
- Contact Person: blank / editable text field
- Business Hours: "asdsfdsaf adfsadfs dff" (sample value displayed)

### Notes
- This indicates the legacy system supports a free-text business-hours/contact field alongside the structured organisation details.
- It is not a coded domain list; it appears to be a free-form text area/entry field.

## Screenshot 5

Source: legacy NOMIS address maintenance popup opened from the "Address Add/Query" button.

### Extracted fields visible on-screen
- NFA
- Flat
- Premises
- Street
- Locality
- Town
- County
- Postal Code
- Country*
- From Date*
- To Date
- Comment
- Type
- Service
- Special Needs
- Primary
- Mail
- Validated PAF

Buttons
- Save
- Exit
- Add
- Query

### Observed sample values
- NFA: blank
- Flat: blank
- Premises: "UVSsUVS"
- Street: "nWslkyHVUtnWslkyHVUt"
- Locality: "PlbVfdPPlbVfdP"
- Town: "Manchester"
- County: "Greater Manchester"
- Postal Code: "A3 6TO"
- Country*: "England"
- From Date*: "04/2016"
- To Date: blank
- Type: "Business Address"
- Service: checked
- Special Needs: blank
- Primary: checked
- Mail: checked
- Validated PAF: blank / unchecked

### Notes
- This modal adds a second, more detailed address model with address lifecycle fields (`From Date`, `To Date`), address classification (`Type`), and flags such as `Primary`, `Mail`, and `Service`.
- The visible data looks like a full address entity with support for PAF validation and address usage flags.

## Comparison with current model
- Status: pending review
- This section will list which extracted fields are supported by the current API/domain model and which are not.

## To do
- Review the remaining "Corporate Types" lookup/button from the main screen, which hasn’t been accessible yet.
- Capture the full set of corporate type values and codes from that popup once available.

## Current status
- The doc is ready for all fields visible on the legacy page captured so far.
- It is not yet fully complete for the old page until the Corporate Types popup/lookup is reviewed, because that area is still inaccessible in the current screenshot set.

## 1:1 legacy field mapping to current database fields

Short answer: most of the legacy NOMIS fields have a mapping to the current database design, but not all are literal 1:1 matches, and a few are represented differently or are not persisted yet.

| Legacy field | Current DB mapping | Notes |
|---|---|---|
| Corporate Name | `organisation.organisation_name` | Direct match |
| Caseload | `organisation.caseload_id` | Direct match |
| Programme No. | `organisation.programme_number` | Direct match |
| VAT | `organisation.vat_number` | Direct match |
| Corporate ID | `organisation.organisation_id` | Generated primary key; not a separate legacy NOMIS corporate ID column; the app does not persist a separate external corporate ID field |
| Comment | `organisation.comments` | Direct match |
| Corporate Types | `organisation_type.organisation_type` + `reference_codes` where `group_code = 'ORGANISATION_TYPE'` | Current design supports multiple organisation types via a dedicated table |
| Active | `organisation.active` | Direct match |
| Expiry Date | `organisation.deactivated_date` | Best match; not a full expiry-date model on the main organisation row |
| Phone Type | `organisation_phone.phone_type` + `reference_codes` where `group_code = 'PHONE_TYPE'` | Matches the `PHONE_USAGE` domain from NOMIS |
| Phone No.* | `organisation_phone.phone_number` | Direct match |
| Extension | `organisation_phone.ext_number` | Direct match |
| Email Address | `organisation_email.email_address` | Direct match |
| Web Address | `organisation_web_address.web_address` | Direct match |
| Email/Web Type | Not stored as a separate column; implicit by table choice (`organisation_email` vs `organisation_web_address`) | The legacy `IADDR_CLASS` values (`EMAIL`, `WEB`) are not persisted as a field in the current schema |
| NFA | `organisation_address.no_fixed_address` | Direct match |
| Flat | `organisation_address.flat` | Direct match |
| Premises/Property | `organisation_address.property` | Direct match |
| Street | `organisation_address.street` | Direct match |
| Locality/Area | `organisation_address.area` | Direct match |
| Town | `organisation_address.city_code` | The current model stores a city code reference, not a plain free-text town value |
| County | `organisation_address.county_code` | Current model stores a county code reference |
| Postal Code | `organisation_address.post_code` | Direct match |
| Country | `organisation_address.country_code` | Current model stores a code reference rather than a free-text country name |
| Type | `organisation_address.address_type` + `reference_codes` where `group_code = 'ADDRESS_TYPE'` | Matches legacy address type values like `BUS`/`HOME` |
| Special Needs | `organisation_address.special_needs_code` + `reference_codes` where `group_code = 'ORG_ADDRESS_SPECIAL_NEEDS'` | Matches the legacy special-needs flag pattern |
| Service | `organisation_address.service_address` | Direct boolean match |
| Primary | `organisation_address.primary_address` | Direct boolean match |
| Mail | `organisation_address.mail_address` | Direct boolean match |
| Contact Person | `organisation_address.contact_person_name` | Direct match |
| Business Hours | `organisation_address.business_hours` | Direct match |
| From Date | `organisation_address.start_date` | Direct match |
| To Date | `organisation_address.end_date` | Direct match |
| Address comments | `organisation_address.comments` | Direct match |
| Validated PAF | No direct column | This is not currently represented in the database schema |
| Association of phone to address | `organisation_address_phone` | Current design supports a phone number being linked to a specific address through the join table |

### Conclusion
- The current schema supports the main legacy NOMIS organisation, address, phone, and email/web data model in a structured way.
- The main gaps are:
  - no explicit persisted field for `Email/Web Type` (it is table-driven rather than a column),
  - no persisted `Validated PAF` field,
  - no separate legacy NOMIS `corporate_id` column beyond the generated `organisation_id`,
  - the Corporate Types popup still needs to be captured to confirm whether all NOMIS corporate type values are fully represented.

## Resolved scope

This comparison is against both of the following:
- the persisted database model (`organisation`, `organisation_address`, `organisation_phone`, `organisation_email`, `organisation_web_address`, etc.), and
- the current POST organisations API model / contract, including the deprecated create-request and response structures used by the application.

This matters because the database supports the nested address/phone/email/web shape, but the current top-level create request model (`CreateOrganisationRequest`) only includes the organisation root fields (`organisationName`, `programmeNumber`, `vatNumber`, `caseloadId`, `comments`, `active`, `deactivatedDate`, audit fields). It does not directly expose all legacy nested address/contact fields in the request body. For the deprecated API to accept the full legacy NOMIS payload, the request model will need to be extended and mapped to the database child tables.

## Open questions
- None for scope: the comparison target is now explicitly both API model and persisted DB model.

## Proposed sample request body

The following illustrates a request shape that preserves the legacy fields captured so far. It is a **proposed example**, not a body accepted by the current deprecated `POST /organisation` endpoint: `CreateOrganisationRequest` currently accepts only the organisation-level fields and audit metadata, not the nested records shown here.

```json
{
  "corporateId": 4594,
  "organisationName": "Example Organisation",
  "caseloadId": "BXI",
  "programmeNumber": "FEI-12345",
  "vatNumber": "GB123456789",
  "comments": "Example organisation comment",
  "organisationTypes": [
    "OTH"
  ],
  "active": true,
  "deactivatedDate": null,
  "phoneNumbers": [],
  "internetAddresses": [
    {
      "type": "EMAIL",
      "address": "contact@example.org"
    },
    {
      "type": "WEB",
      "address": "https://www.example.org"
    }
  ],
  "addresses": [
    {
      "noFixedAddress": false,
      "flat": null,
      "property": "Example House",
      "street": "Example Street",
      "area": "Example Locality",
      "cityCode": "25343",
      "countyCode": "MANCHESTER",
      "postCode": "A3 6TO",
      "countryCode": "ENG",
      "startDate": "2016-04-01",
      "endDate": null,
      "comments": null,
      "addressType": "BUS",
      "serviceAddress": true,
      "specialNeedsCode": null,
      "primaryAddress": true,
      "mailAddress": true,
      "active": true,
      "validatedPaf": false,
      "contactPersonName": "Example Contact",
      "businessHours": "Monday to Friday, 09:00 to 17:00",
      "phoneNumbers": [
        {
          "phoneType": "BUS",
          "phoneNumber": "01632 960123",
          "extNumber": null
        }
      ]
    }
  ],
  "createdBy": "example-user",
  "createdTime": "2026-10-07T12:00:00",
  "updatedBy": null,
  "updatedTime": null
}
```

Notes:
- `corporateId` preserves the legacy identifier; the current database uses `organisation_id` and does not store a separate NOMIS corporate ID.
- `internetAddresses` keeps the legacy `IADDR_CLASS` type (`EMAIL` or `WEB`) explicit. The current database instead separates these into `organisation_email` and `organisation_web_address`.
- `active` on an address and `validatedPaf` are included because they appear on the legacy screen, but neither currently has a corresponding `organisation_address` database column.
- The city/county reference-code examples are illustrative; use valid codes from the deployed reference data. The `OTH` organisation type is only a placeholder and must be checked against the Corporate Types lookup when available.
- `phoneNumbers` at the organisation root represents direct organisation phones; address-specific numbers are nested under each address. Audit metadata for nested records is omitted from this illustrative body.
