package pme123.adapters.server.entity

sealed trait ClientType

case object JOB_CLIENT extends ClientType

case object RESULT_CLIENT extends ClientType
