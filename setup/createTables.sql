BEGIN;

CREATE TABLE "public"."reminders" ( 
  "author" Character Varying( 200 ) NOT NULL,
  "recipient" Character Varying( 200 ) NOT NULL,
  "message" Character Varying( 2044 ) NOT NULL,
  "id" Character Varying( 40 ) NOT NULL,
  "schedules" Text NOT NULL,
  PRIMARY KEY ( "id" ) );
;

CREATE INDEX "index_author" ON "public"."reminders" USING btree( "author" Asc NULLS Last );
CREATE INDEX "index_recipient" ON "public"."reminders" USING btree( "recipient" Asc NULLS Last );

ALTER TABLE public.reminders
    OWNER to matterminder;

GRANT ALL ON TABLE public.reminders TO matterminder;

COMMIT;