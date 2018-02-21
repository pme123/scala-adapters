package pme123.adapters.server.entity

sealed trait ClientType

case object JOB_CLIENT extends ClientType

case object JOB_RESULTS extends ClientType

case object CUSTOM_PAGE extends ClientType
