db:
	docker-compose -f docker-compose-db.yml up -d
app:
	docker-compose -f docker-compose-app.yml up

db_down:
	docker-compose -v -f docker-compose-db.yml down
app_down:
	docker-compose -v -f docker-compose-app.yml down

all_down:
	docker-compose -v -f docker-compose-db.yml down
	docker-compose -v -f docker-compose-app.yml down

all_clean:
	docker-compose -v -f docker-compose-db.yml down --rmi all --remove-orphans
	docker-compose -v -f docker-compose-app.yml down --rmi all --remove-orphans

